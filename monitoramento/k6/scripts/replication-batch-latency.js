import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import {
  readReplicationConfig,
  registerReplicationOutcome,
  markReplicationFailure,
} from '../helpers/replication.js';

const BASE_URLS = {
  graduacao: __ENV.GRADUACAO_URL || 'http://localhost:8081',
  posGraduacao: __ENV.POS_GRADUACAO_URL || 'http://localhost:8082',
  diplomas: __ENV.DIPLOMAS_URL || 'http://localhost:8083',
  assinatura: __ENV.ASSINATURA_URL || 'http://localhost:8084',
};

function readIntEnv(name, fallback, min = 0) {
  const raw = __ENV[name];
  if (raw === undefined || raw === '') {
    return fallback;
  }

  const parsed = Number(raw);
  if (!Number.isFinite(parsed) || !Number.isInteger(parsed) || parsed < min) {
    fail(`Valor invalido para ${name}: ${raw}`);
  }

  return parsed;
}

function readBoolEnv(name, fallback) {
  const raw = __ENV[name];
  if (raw === undefined || raw === '') {
    return fallback;
  }

  const normalized = String(raw).toLowerCase().trim();
  if (normalized === 'true' || normalized === '1' || normalized === 'yes') {
    return true;
  }
  if (normalized === 'false' || normalized === '0' || normalized === 'no') {
    return false;
  }

  fail(`Valor invalido para ${name}: ${raw}`);
  return fallback;
}

function readListEnv(name, fallbackList) {
  const raw = __ENV[name];
  if (!raw) {
    return [...fallbackList];
  }

  return raw
    .split(',')
    .map((value) => value.trim())
    .filter((value) => value.length > 0);
}

function validateTargets(name, targets) {
  for (const target of targets) {
    if (!BASE_URLS[target]) {
      fail(`Destino invalido em ${name}: ${target}`);
    }
  }
}

function parseJson(body) {
  try {
    return JSON.parse(body);
  } catch {
    return null;
  }
}

function buildOperationStats(requested) {
  return {
    requested,
    success: 0,
    failure: 0,
    skipped: 0,
  };
}

function percentile(values, percentileValue) {
  if (values.length === 0) {
    return null;
  }

  const sorted = [...values].sort((a, b) => a - b);
  const index = Math.ceil((percentileValue / 100) * sorted.length) - 1;
  const normalizedIndex = Math.min(sorted.length - 1, Math.max(0, index));
  return sorted[normalizedIndex];
}

function formatLatency(value) {
  if (value === null || value === undefined) {
    return '-';
  }
  return value.toFixed(0);
}

const PESSOA_CREATE_COUNT = readIntEnv('BULK_PESSOA_CREATE_COUNT', 100, 0);
const PESSOA_UPDATE_COUNT = readIntEnv('BULK_PESSOA_UPDATE_COUNT', 100, 0);
const ENABLE_VINCULO_CREATE = readBoolEnv('BULK_ENABLE_VINCULO', true);
const SCENARIO_MAX_DURATION = __ENV.BULK_MAX_DURATION || '45m';

const PESSOA_CREATE_TARGETS = readListEnv('BULK_PESSOA_CREATE_TARGETS', ['posGraduacao', 'diplomas', 'assinatura']);
const PESSOA_UPDATE_TARGETS = readListEnv('BULK_PESSOA_UPDATE_TARGETS', ['diplomas']);
const VINCULO_TARGETS = ENABLE_VINCULO_CREATE
  ? readListEnv('BULK_VINCULO_TARGETS', ['diplomas'])
  : [];

validateTargets('BULK_PESSOA_CREATE_TARGETS', PESSOA_CREATE_TARGETS);
validateTargets('BULK_PESSOA_UPDATE_TARGETS', PESSOA_UPDATE_TARGETS);
if (ENABLE_VINCULO_CREATE) {
  validateTargets('BULK_VINCULO_TARGETS', VINCULO_TARGETS);
}

const replicationConfig = readReplicationConfig({
  defaults: {
    timeoutMs: 60000,
    pollIntervalMs: 1000,
    mode: 'strict',
    sampleRate: 1,
  },
});

const ITEM_TIMEOUT_MS = readIntEnv('BULK_ITEM_TIMEOUT_MS', replicationConfig.timeoutMs, 1);
const POLL_INTERVAL_MS = readIntEnv('BULK_POLL_INTERVAL_MS', replicationConfig.pollIntervalMs, 1);

const headers = { 'Content-Type': 'application/json' };
const createdPessoas = [];
const pendingChecks = [];
const summaryRows = {};

const operationStats = {
  pessoa_create: buildOperationStats(PESSOA_CREATE_COUNT),
  vinculo_create: buildOperationStats(ENABLE_VINCULO_CREATE ? PESSOA_CREATE_COUNT : 0),
  pessoa_update: buildOperationStats(PESSOA_UPDATE_COUNT),
};

function summaryRowKey(entity, action, source, target) {
  return `${entity}|${action}|${source}|${target}`;
}

function ensureSummaryRow({ entity, action, source, target }) {
  const key = summaryRowKey(entity, action, source, target);
  if (!summaryRows[key]) {
    summaryRows[key] = {
      entity,
      action,
      source,
      target,
      expected: 0,
      success: 0,
      failure: 0,
      latencies: [],
    };
  }

  return summaryRows[key];
}

function queueReplicationCheck({
  entity,
  action,
  source,
  target,
  pessoaId,
  originAt,
  url,
  validateFn,
  successLabel,
  dataLabel,
  dataValidator,
  endpointTag,
}) {
  const row = ensureSummaryRow({ entity, action, source, target });
  row.expected += 1;

  pendingChecks.push({
    rowKey: summaryRowKey(entity, action, source, target),
    entity,
    action,
    source,
    target,
    pessoaId,
    originAt,
    deadlineAt: originAt + ITEM_TIMEOUT_MS,
    url,
    endpointTag,
    validateFn,
    successLabel,
    dataLabel,
    dataValidator,
    tags: {
      entity,
      action,
      source,
      target,
    },
    attempts: 0,
    done: false,
  });
}

function finalizePendingCheck(pendingCheck, result) {
  if (pendingCheck.done) {
    return;
  }

  pendingCheck.done = true;

  const passed = registerReplicationOutcome({
    result,
    successLabel: pendingCheck.successLabel,
    dataLabel: pendingCheck.dataLabel,
    dataValidator: pendingCheck.dataValidator,
    tags: pendingCheck.tags,
  });

  const row = summaryRows[pendingCheck.rowKey];
  if (!row) {
    return;
  }

  if (passed) {
    row.success += 1;
    row.latencies.push(result.latency);
  } else {
    row.failure += 1;
  }
}

function buildPessoaPayload(index) {
  const timestamp = Date.now();
  const uniqueBase = `${timestamp}${index}${Math.floor(Math.random() * 1000)}`;
  const cpf = uniqueBase.slice(-11).padStart(11, '0');

  return {
    nome: `Batch User ${timestamp}-${index}`,
    dataNascimento: '1995-05-15',
    nomeSocial: null,
    documentoIdentificacao: {
      tipo: 'CPF',
      numero: cpf,
    },
    contato: {
      email: `batch-${timestamp}-${index}@example.com`,
      telefone: '11987654321',
    },
    endereco: {
      logradouro: 'Rua Teste, 123',
      cidade: 'Sao Paulo',
      uf: 'SP',
      cep: '01234-567',
    },
  };
}

function loadCursoTurmaForVinculo() {
  const cursosResponse = http.get(`${BASE_URLS.graduacao}/cursos`, {
    tags: { service: 'graduacao', endpoint: 'GET /cursos' },
  });
  if (cursosResponse.status !== 200) {
    fail(`Falha ao buscar cursos para vinculo: ${cursosResponse.status}`);
  }

  const cursos = parseJson(cursosResponse.body);
  if (!Array.isArray(cursos) || cursos.length === 0) {
    fail('Nenhum curso encontrado para criar vinculos.');
  }

  const curso = cursos[0];
  const turmasResponse = http.get(`${BASE_URLS.graduacao}/cursos/${curso.id}/turmas`, {
    tags: { service: 'graduacao', endpoint: 'GET /cursos/{id}/turmas' },
  });
  if (turmasResponse.status !== 200) {
    fail(`Falha ao buscar turmas para vinculo: ${turmasResponse.status}`);
  }

  const turmas = parseJson(turmasResponse.body);
  if (!Array.isArray(turmas) || turmas.length === 0) {
    fail('Nenhuma turma encontrada para criar vinculos.');
  }

  return {
    cursoId: curso.id,
    turmaId: turmas[0].id,
  };
}

function runWritePhase() {
  console.log('='.repeat(70));
  console.log('FASE 1 - CARGA DE ESCRITA');
  console.log('='.repeat(70));
  console.log(`Pessoas novas solicitadas: ${PESSOA_CREATE_COUNT}`);
  console.log(`Atualizacoes de pessoa solicitadas: ${PESSOA_UPDATE_COUNT}`);
  console.log(`Criar vinculo para cada pessoa nova: ${ENABLE_VINCULO_CREATE}`);
  console.log(`Timeout por item de replicacao: ${ITEM_TIMEOUT_MS}ms`);
  console.log(`Polling da fase 2: ${POLL_INTERVAL_MS}ms`);
  console.log('='.repeat(70));

  let turmaSeed = null;
  if (ENABLE_VINCULO_CREATE && PESSOA_CREATE_COUNT > 0) {
    turmaSeed = loadCursoTurmaForVinculo();
    console.log(`Turma selecionada para vinculo: ${turmaSeed.turmaId}`);
  }

  for (let i = 1; i <= PESSOA_CREATE_COUNT; i += 1) {
    const pessoaPayload = buildPessoaPayload(i);
    const createResponse = http.post(
      `${BASE_URLS.graduacao}/pessoas`,
      JSON.stringify(pessoaPayload),
      { headers, tags: { service: 'graduacao', endpoint: 'POST /pessoas' } },
    );

    const createCheck = check(createResponse, {
      'batch: pessoa criada (201)': (r) => r.status === 201,
    });

    if (!createCheck) {
      operationStats.pessoa_create.failure += 1;
      markReplicationFailure({ entity: 'pessoa', action: 'create_write', source: 'graduacao' });
      if (ENABLE_VINCULO_CREATE) {
        operationStats.vinculo_create.skipped += 1;
      }
      continue;
    }

    const pessoaCriada = parseJson(createResponse.body);
    if (!pessoaCriada || pessoaCriada.id === undefined) {
      operationStats.pessoa_create.failure += 1;
      markReplicationFailure({ entity: 'pessoa', action: 'create_write', source: 'graduacao' });
      if (ENABLE_VINCULO_CREATE) {
        operationStats.vinculo_create.skipped += 1;
      }
      continue;
    }

    operationStats.pessoa_create.success += 1;
    createdPessoas.push({
      id: pessoaCriada.id,
      nome: pessoaCriada.nome ?? pessoaPayload.nome,
      dataNascimento: pessoaCriada.dataNascimento ?? pessoaPayload.dataNascimento,
    });

    const pessoaCreateAt = Date.now();
    for (const target of PESSOA_CREATE_TARGETS) {
      queueReplicationCheck({
        entity: 'pessoa',
        action: 'create',
        source: 'graduacao',
        target,
        pessoaId: pessoaCriada.id,
        originAt: pessoaCreateAt,
        url: `${BASE_URLS[target]}/pessoas/${pessoaCriada.id}`,
        endpointTag: 'GET /pessoas/{id}',
        validateFn: (data) => data.id === pessoaCriada.id
          && data.nome === (pessoaCriada.nome ?? pessoaPayload.nome)
          && data.dataNascimento === (pessoaCriada.dataNascimento ?? pessoaPayload.dataNascimento),
        successLabel: 'batch: pessoa create replicada',
        dataLabel: 'batch: pessoa create consistente',
        dataValidator: (data) => data.id === pessoaCriada.id,
      });
    }

    if (!ENABLE_VINCULO_CREATE || !turmaSeed) {
      continue;
    }

    const alunoPayload = JSON.stringify({
      pessoaId: pessoaCriada.id,
      turmaId: turmaSeed.turmaId,
      dataMatricula: new Date().toISOString().split('T')[0],
      status: 'ATIVO',
    });

    const vinculoResponse = http.post(
      `${BASE_URLS.graduacao}/alunos`,
      alunoPayload,
      { headers, tags: { service: 'graduacao', endpoint: 'POST /alunos' } },
    );

    const vinculoWriteCheck = check(vinculoResponse, {
      'batch: vinculo criado (201)': (r) => r.status === 201,
    });

    if (!vinculoWriteCheck) {
      operationStats.vinculo_create.failure += 1;
      markReplicationFailure({ entity: 'vinculo', action: 'create_write', source: 'graduacao' });
      continue;
    }

    operationStats.vinculo_create.success += 1;
    const vinculoCreateAt = Date.now();
    for (const target of VINCULO_TARGETS) {
      queueReplicationCheck({
        entity: 'vinculo',
        action: 'create',
        source: 'graduacao',
        target,
        pessoaId: pessoaCriada.id,
        originAt: vinculoCreateAt,
        url: `${BASE_URLS[target]}/vinculos`,
        endpointTag: 'GET /vinculos',
        validateFn: (data) => Array.isArray(data) && data.some((v) => v.pessoaId === pessoaCriada.id),
        successLabel: 'batch: vinculo create replicado',
        dataLabel: '',
        dataValidator: undefined,
      });
    }
  }

  if (createdPessoas.length === 0) {
    operationStats.pessoa_update.skipped = operationStats.pessoa_update.requested;
    return;
  }

  const effectiveUpdateCount = Math.min(PESSOA_UPDATE_COUNT, createdPessoas.length);
  operationStats.pessoa_update.skipped = PESSOA_UPDATE_COUNT - effectiveUpdateCount;

  for (let i = 0; i < effectiveUpdateCount; i += 1) {
    const pessoa = createdPessoas[i];
    const nomeSocial = `NomeSocial Batch ${Date.now()}-${i + 1}`;

    const updatePayload = JSON.stringify({
      nome: pessoa.nome,
      dataNascimento: pessoa.dataNascimento,
      nomeSocial,
    });

    const updateResponse = http.put(
      `${BASE_URLS.graduacao}/pessoas/${pessoa.id}`,
      updatePayload,
      { headers, tags: { service: 'graduacao', endpoint: 'PUT /pessoas/{id}' } },
    );

    const updateCheck = check(updateResponse, {
      'batch: pessoa atualizada (200)': (r) => r.status === 200,
    });

    if (!updateCheck) {
      operationStats.pessoa_update.failure += 1;
      markReplicationFailure({ entity: 'pessoa', action: 'update_write', source: 'graduacao' });
      continue;
    }

    operationStats.pessoa_update.success += 1;
    const pessoaUpdateAt = Date.now();
    for (const target of PESSOA_UPDATE_TARGETS) {
      queueReplicationCheck({
        entity: 'pessoa',
        action: 'update',
        source: 'graduacao',
        target,
        pessoaId: pessoa.id,
        originAt: pessoaUpdateAt,
        url: `${BASE_URLS[target]}/pessoas/${pessoa.id}`,
        endpointTag: 'GET /pessoas/{id}',
        validateFn: (data) => data.nomeSocial === nomeSocial,
        successLabel: 'batch: pessoa update replicada',
        dataLabel: 'batch: pessoa update consistente',
        dataValidator: (data) => data.nomeSocial === nomeSocial,
      });
    }
  }
}

function runReplicationValidationPhase() {
  console.log('\n' + '='.repeat(70));
  console.log('FASE 2 - VALIDACAO DE REPLICACAO (FILA PENDENTE)');
  console.log('='.repeat(70));
  console.log(`Checks pendentes para validar: ${pendingChecks.length}`);

  let cycle = 0;
  let resolved = 0;
  let remaining = pendingChecks.length;

  while (remaining > 0) {
    cycle += 1;
    const cycleStartedAt = Date.now();
    let resolvedThisCycle = 0;

    for (const pendingCheck of pendingChecks) {
      if (pendingCheck.done) {
        continue;
      }

      const now = Date.now();
      if (now >= pendingCheck.deadlineAt) {
        finalizePendingCheck(pendingCheck, {
          success: false,
          attempts: pendingCheck.attempts,
          latency: now - pendingCheck.originAt,
          data: null,
        });
        resolved += 1;
        remaining -= 1;
        resolvedThisCycle += 1;
        continue;
      }

      pendingCheck.attempts += 1;
      const response = http.get(pendingCheck.url, {
        tags: { service: pendingCheck.target, endpoint: pendingCheck.endpointTag },
      });

      if (response.status === 200) {
        const data = parseJson(response.body);
        if (data !== null && pendingCheck.validateFn(data, response)) {
          finalizePendingCheck(pendingCheck, {
            success: true,
            attempts: pendingCheck.attempts,
            latency: Date.now() - pendingCheck.originAt,
            data,
          });
          resolved += 1;
          remaining -= 1;
          resolvedThisCycle += 1;
          continue;
        }
      }

      if (!pendingCheck.done && Date.now() >= pendingCheck.deadlineAt) {
        finalizePendingCheck(pendingCheck, {
          success: false,
          attempts: pendingCheck.attempts,
          latency: Date.now() - pendingCheck.originAt,
          data: null,
        });
        resolved += 1;
        remaining -= 1;
        resolvedThisCycle += 1;
      }
    }

    if (cycle === 1 || cycle % 10 === 0 || resolvedThisCycle > 0 || remaining === 0) {
      console.log(`[fase2][ciclo ${cycle}] resolvidas=${resolved}/${pendingChecks.length} pendentes=${remaining} resolvidas_no_ciclo=${resolvedThisCycle}`);
    }

    if (remaining > 0) {
      const elapsed = Date.now() - cycleStartedAt;
      const sleepSeconds = Math.max(0, (POLL_INTERVAL_MS - elapsed) / 1000);
      if (sleepSeconds > 0) {
        sleep(sleepSeconds);
      }
    }
  }
}

function printWriteSummaryTable() {
  console.log('\nTabela de escrita:');
  console.log('| OPERACAO | SOLICITADO | SUCESSO | FALHA | PULADO |');
  console.log('| --- | --- | --- | --- | --- |');

  for (const [operation, stats] of Object.entries(operationStats)) {
    console.log(`| ${operation.toUpperCase()} | ${stats.requested} | ${stats.success} | ${stats.failure} | ${stats.skipped} |`);
  }
}

function printReplicationSummaryTable() {
  const rows = Object.values(summaryRows).sort((a, b) => {
    const first = `${a.entity}|${a.action}|${a.source}|${a.target}`;
    const second = `${b.entity}|${b.action}|${b.source}|${b.target}`;
    return first.localeCompare(second);
  });

  console.log('\nTabela de replicacao e latencia:');
  console.log('| ENTIDADE | ACAO | ORIGEM | DESTINO | QUANTIDADE | SUCESSO | FALHA | P50_MS | P95_MS | P99_MS | MAX_MS |');
  console.log('| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |');

  for (const row of rows) {
    const p50 = percentile(row.latencies, 50);
    const p95 = percentile(row.latencies, 95);
    const p99 = percentile(row.latencies, 99);
    const max = row.latencies.length > 0 ? Math.max(...row.latencies) : null;

    console.log(`| ${row.entity.toUpperCase()} | ${row.action.toUpperCase()} | ${row.source.toUpperCase()} | ${row.target.toUpperCase()} | ${row.expected} | ${row.success} | ${row.failure} | ${formatLatency(p50)} | ${formatLatency(p95)} | ${formatLatency(p99)} | ${formatLatency(max)} |`);
  }
}

export const options = {
  scenarios: {
    replication_batch_latency: {
      executor: 'shared-iterations',
      vus: 1,
      iterations: 1,
      maxDuration: SCENARIO_MAX_DURATION,
    },
  },
  thresholds: {
    checks: ['rate>0.95'],
    http_req_duration: ['p(95)<8000'],
  },
};

export default function () {
  runWritePhase();
  runReplicationValidationPhase();

  console.log('\n' + '='.repeat(70));
  console.log('RESUMO FINAL');
  console.log('='.repeat(70));
  console.log(`Pessoas criadas com sucesso: ${operationStats.pessoa_create.success}/${operationStats.pessoa_create.requested}`);
  console.log(`Vinculos criados com sucesso: ${operationStats.vinculo_create.success}/${operationStats.vinculo_create.requested}`);
  console.log(`Pessoas atualizadas com sucesso: ${operationStats.pessoa_update.success}/${operationStats.pessoa_update.requested}`);
  console.log(`Checks de replicacao enfileirados: ${pendingChecks.length}`);
  printWriteSummaryTable();
  printReplicationSummaryTable();
  console.log('='.repeat(70));
}

export function handleSummary(data) {
  const successCount = data.metrics.replication_success_total?.values.count || 0;
  const failureCount = data.metrics.replication_failure_total?.values.count || 0;
  const avgLatency = data.metrics.replication_latency_ms?.values.avg || 0;
  const p95Latency = data.metrics.replication_latency_ms?.values['p(95)'] || 0;
  const p99Latency = data.metrics.replication_latency_ms?.values['p(99)'] || 0;

  console.log('\n' + '='.repeat(70));
  console.log('METRICAS AGREGADAS');
  console.log('='.repeat(70));
  console.log(`Replicacoes bem-sucedidas: ${successCount}`);
  console.log(`Replicacoes falhadas: ${failureCount}`);
  console.log(`Latencia media: ${avgLatency.toFixed(2)}ms`);
  console.log(`Latencia P95: ${p95Latency.toFixed(2)}ms`);
  console.log(`Latencia P99: ${p99Latency.toFixed(2)}ms`);
  console.log('='.repeat(70));

  return {};
}
