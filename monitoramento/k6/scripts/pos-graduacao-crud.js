import { sleep, fail } from 'k6';
import { Trend } from 'k6/metrics';
import { loadEnvFile, makeEnvReaders } from '../helpers/dotenv.js';
import { loadConfig } from '../helpers/config.js';
import { buildOptions } from '../helpers/options.js';
import { runPosGraduacaoFlow } from '../helpers/business-flows.js';

const envFilePath = __ENV.ENV_FILE || '.env';
const fileEnv = loadEnvFile([envFilePath]);
const { envValue, envNumber, envJson } = makeEnvReaders({ envVars: __ENV, envFile: fileEnv });

// Script de teste inicial do servico de pos-graduacao (CRUD + fluxo basico).
const config = loadConfig();
const scenario = envValue('SCENARIO', 'pos-graduacao-crud');
const runId = envValue('RUN_ID', `run_${Date.now()}`);
const baseUrl = envValue(
  'POS_GRADUACAO_BASE_URL',
  envValue(
    'POS_GRADUACAO_URL',
    (config.baseUrls && (config.baseUrls.posGraduacao || config.baseUrls['pos-graduacao'])) || 'http://localhost:8082',
  ),
);

if (!baseUrl) {
  fail('Base URL não configurada para pos-graduacao. Use POS_GRADUACAO_BASE_URL ou config baseUrls.posGraduacao.');
}

// Duração total do fluxo de pós-graduação por iteração (métrica de tempo de resposta de ponta a ponta).
const flowDuration = new Trend('pos_graduacao_flow_duration', true);

export const options = buildOptions({ envValue, envNumber, envJson, runId, scenario });

export default function () {
  const startedAt = Date.now();
  runPosGraduacaoFlow({ baseUrl, runId, scenario });

  flowDuration.add(Date.now() - startedAt, { service: 'pos-graduacao' });
  const sleepSeconds = envNumber('SLEEP_S', envNumber('K6_SLEEP_S', 1));
  if (sleepSeconds > 0) {
    sleep(sleepSeconds);
  }
}
