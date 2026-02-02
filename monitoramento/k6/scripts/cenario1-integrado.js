import http from 'k6/http';
import { check, sleep, fail } from 'k6';
import { Trend } from 'k6/metrics';
import { loadEnvFile, makeEnvReaders } from '../helpers/dotenv.js';
import { loadConfig } from '../helpers/config.js';
import { buildOptions } from '../helpers/options.js';
import { hashString, uniqueSeed } from '../helpers/util.js';
import { runGraduacaoFlow, runPosGraduacaoFlow } from '../helpers/business-flows.js';
import {
  waitForReplication,
  registerReplicationOutcome,
  markReplicationFailure,
  readReplicationConfig,
  shouldRunReplicationChecks,
} from '../helpers/replication.js';

const envFilePath = __ENV.ENV_FILE || '.env';
const fileEnv = loadEnvFile([envFilePath]);
const { envValue, envNumber, envJson } = makeEnvReaders({ envVars: __ENV, envFile: fileEnv });

const config = loadConfig();
const scenario = envValue('SCENARIO', 'cenario1-integrado');
const runId = envValue('RUN_ID', `run_${Date.now()}`);

function resolveBaseUrl({ primaryEnv, secondaryEnv, configKeys, fallback }) {
  const first = envValue(primaryEnv, '');
  if (first) {
    return first;
  }

  if (secondaryEnv) {
    const second = envValue(secondaryEnv, '');
    if (second) {
      return second;
    }
  }

  if (config.baseUrls) {
    for (const key of configKeys) {
      if (config.baseUrls[key]) {
        return config.baseUrls[key];
      }
    }
  }

  return fallback;
}

const baseUrls = {
  graduacao: resolveBaseUrl({
    primaryEnv: 'GRADUACAO_BASE_URL',
    secondaryEnv: 'GRADUACAO_URL',
    configKeys: ['graduacao'],
    fallback: 'http://localhost:8081',
  }),
  posGraduacao: resolveBaseUrl({
    primaryEnv: 'POS_GRADUACAO_BASE_URL',
    secondaryEnv: 'POS_GRADUACAO_URL',
    configKeys: ['posGraduacao', 'pos-graduacao'],
    fallback: 'http://localhost:8082',
  }),
  diplomas: resolveBaseUrl({
    primaryEnv: 'DIPLOMAS_BASE_URL',
    secondaryEnv: 'DIPLOMAS_URL',
    configKeys: ['diplomas'],
    fallback: 'http://localhost:8083',
  }),
  assinatura: resolveBaseUrl({
    primaryEnv: 'ASSINATURA_BASE_URL',
    secondaryEnv: 'ASSINATURA_URL',
    configKeys: ['assinatura'],
    fallback: 'http://localhost:8084',
  }),
};

for (const [serviceName, baseUrl] of Object.entries(baseUrls)) {
  if (!baseUrl) {
    fail(`Base URL nao configurada para ${serviceName}.`);
  }
}

const replicationConfig = readReplicationConfig({
  envValue,
  envNumber,
  defaults: {
    timeoutMs: 30000,
    pollIntervalMs: 500,
    mode: 'sampled',
    sampleRate: 0.2,
  },
});

const flowDuration = new Trend('cenario1_integrado_flow_duration', true);

const baseOptions = buildOptions({ envValue, envNumber, envJson, runId, scenario });
const replicationFailureThreshold = envValue('K6_REPLICATION_FAILURE_TOTAL', '');

export const options = {
  ...baseOptions,
  thresholds: {
    ...baseOptions.thresholds,
    ...(replicationFailureThreshold ? { replication_failure_total: [replicationFailureThreshold] } : {}),
  },
};

function params(service, endpoint) {
  return {
    headers: {
      'Content-Type': 'application/json',
      'X-Run-Id': runId,
      'X-Scenario': scenario,
    },
    tags: {
      service,
      endpoint,
    },
  };
}

function validatePessoaReplication({ sourceService, pessoa, targets }) {
  let allOk = true;

  for (const target of targets) {
    const result = waitForReplication({
      url: `${baseUrls[target]}/pessoas/${pessoa.id}`,
      validateFn: (data) => data.id === pessoa.id
        && data.nome === pessoa.nome
        && data.dataNascimento === pessoa.dataNascimento,
      maxAttempts: replicationConfig.maxAttempts,
      intervalMs: replicationConfig.pollIntervalMs,
    });

    const ok = registerReplicationOutcome({
      result,
      successLabel: `${sourceService}->${target}: Pessoa replicada`,
      dataLabel: `${sourceService}->${target}: Pessoa consistente`,
      dataValidator: (data) => data.nome === pessoa.nome,
      tags: {
        entity: 'pessoa',
        source: sourceService,
        target,
      },
    });

    if (!ok) {
      allOk = false;
    }
  }

  return allOk;
}

function validateVinculoReplication({ sourceService, pessoaId }) {
  const result = waitForReplication({
    url: `${baseUrls.diplomas}/vinculos`,
    validateFn: (data) => Array.isArray(data) && data.some((v) => v.pessoaId === pessoaId),
    maxAttempts: replicationConfig.maxAttempts,
    intervalMs: replicationConfig.pollIntervalMs,
  });

  return registerReplicationOutcome({
    result,
    successLabel: `${sourceService}->diplomas: Vinculo replicado`,
    tags: {
      entity: 'vinculo',
      source: sourceService,
      target: 'diplomas',
    },
  });
}

function validatePessoaUpdateReplication({ sourceService, sourceBaseUrl, pessoa }) {
  const novoNomeSocial = `Nome Social ${sourceService} ${Date.now()}`;
  const payload = JSON.stringify({
    nome: pessoa.nome,
    dataNascimento: pessoa.dataNascimento,
    nomeSocial: novoNomeSocial,
  });

  const updateResponse = http.put(
    `${sourceBaseUrl}/pessoas/${pessoa.id}`,
    payload,
    params(sourceService, 'PUT /pessoas/{id}'),
  );

  const updated = check(updateResponse, {
    [`${sourceService}: Pessoa atualizada`]: (r) => r.status === 200,
  });

  if (!updated) {
    markReplicationFailure({
      entity: 'pessoa_update',
      source: sourceService,
      target: 'diplomas',
    });
    return false;
  }

  const result = waitForReplication({
    url: `${baseUrls.diplomas}/pessoas/${pessoa.id}`,
    validateFn: (data) => data.nomeSocial === novoNomeSocial,
    maxAttempts: replicationConfig.maxAttempts,
    intervalMs: replicationConfig.pollIntervalMs,
  });

  return registerReplicationOutcome({
    result,
    successLabel: `${sourceService}->diplomas: Atualizacao replicada`,
    dataLabel: `${sourceService}->diplomas: nomeSocial consistente`,
    dataValidator: (data) => data.nomeSocial === novoNomeSocial,
    tags: {
      entity: 'pessoa_update',
      source: sourceService,
      target: 'diplomas',
    },
  });
}

export default function () {
  const startedAt = Date.now();
  const seed = uniqueSeed(__VU, __ITER) + (hashString(runId) % 100000);

  const graduacaoFlow = runGraduacaoFlow({
    baseUrl: baseUrls.graduacao,
    runId,
    scenario,
    seed,
  });

  const posFlow = runPosGraduacaoFlow({
    baseUrl: baseUrls.posGraduacao,
    runId,
    scenario,
    seed: seed + 17,
  });

  const runReplicationChecks = shouldRunReplicationChecks({
    mode: replicationConfig.mode,
    sampleRate: replicationConfig.sampleRate,
    seed: seed + 101,
  });

  if (runReplicationChecks) {
    validatePessoaReplication({
      sourceService: 'graduacao',
      pessoa: graduacaoFlow.pessoaAluno,
      targets: ['posGraduacao', 'diplomas', 'assinatura'],
    });

    validateVinculoReplication({
      sourceService: 'graduacao',
      pessoaId: graduacaoFlow.pessoaAluno.id,
    });

    validatePessoaUpdateReplication({
      sourceService: 'graduacao',
      sourceBaseUrl: baseUrls.graduacao,
      pessoa: graduacaoFlow.pessoaAluno,
    });

    validatePessoaReplication({
      sourceService: 'pos-graduacao',
      pessoa: posFlow.pessoaAluno,
      targets: ['graduacao', 'diplomas', 'assinatura'],
    });

    validateVinculoReplication({
      sourceService: 'pos-graduacao',
      pessoaId: posFlow.pessoaAluno.id,
    });

    validatePessoaUpdateReplication({
      sourceService: 'pos-graduacao',
      sourceBaseUrl: baseUrls.posGraduacao,
      pessoa: posFlow.pessoaAluno,
    });
  }

  flowDuration.add(Date.now() - startedAt, { service: 'cenario1-integrado' });

  const sleepSeconds = envNumber('SLEEP_S', envNumber('K6_SLEEP_S', 1));
  if (sleepSeconds > 0) {
    sleep(sleepSeconds);
  }
}
