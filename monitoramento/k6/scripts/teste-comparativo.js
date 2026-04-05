/**
 * teste-comparativo.js
 *
 * Script k6 unificado para comparação de cenários C1/C2A1/C2A2/C2A3.
 * Mede M1-M5 com métricas customizadas exportadas ao Prometheus via remote write.
 *
 * Uso:
 *   k6 run monitoramento/k6/scripts/teste-comparativo.js \
 *     --tag scenario=c1 --tag run_id=run_20260404_120000 \
 *     --out experimental-prometheus-rw=http://localhost:9090/api/v1/write
 *
 * Variáveis de ambiente:
 *   INTENSITY        low|medium|high (default: low)
 *   VUS              override manual de VUs
 *   DURATION         override manual de duração (e.g. '10m')
 *   SLEEP_S          pausa entre iterações em segundos (default: 1)
 *   GRADUACAO_URL    (default: http://localhost:8081)
 *   POS_GRADUACAO_URL (default: http://localhost:8082)
 *   DIPLOMAS_URL     (default: http://localhost:8083)
 *   ASSINATURA_URL   (default: http://localhost:8084)
 *   REPLICATION_TIMEOUT_MS  (default: 120000)
 *   POLL_INTERVAL_MS        (default: 1000)
 *   REPLICATION_MODE        off|sampled|strict (default: strict)
 */

import http from 'k6/http';
import { sleep, check } from 'k6';
import { runGraduacaoFlow, runPosGraduacaoFlow } from '../helpers/business-flows.js';
import {
  waitForReplication,
  readReplicationConfig,
  replicationLatency,
  replicationSuccess,
  replicationFailure,
} from '../helpers/replication.js';
import { businessThroughput, stalenessWindow } from '../helpers/metrics.js';
import { alphaCode, isoDate } from '../helpers/util.js';

// ── Configuração ─────────────────────────────────────────────────────────────

const BASE_URLS = {
  graduacao: __ENV.GRADUACAO_URL || 'http://localhost:8081',
  posGraduacao: __ENV.POS_GRADUACAO_URL || 'http://localhost:8082',
  diplomas: __ENV.DIPLOMAS_URL || 'http://localhost:8083',
  assinatura: __ENV.ASSINATURA_URL || 'http://localhost:8084',
};

const INTENSITY = __ENV.INTENSITY || 'low';
const IS_CDC = (__ENV.WARMUP_S && parseInt(__ENV.WARMUP_S, 10) > 0);
const CONFIGS = {
  low: { vus: IS_CDC ? 2 : 5, duration: '5m' },
  medium: { vus: IS_CDC ? 5 : 20, duration: '10m' },
  high: { vus: IS_CDC ? 10 : 50, duration: '15m' },
};
const intensityCfg = CONFIGS[INTENSITY] || CONFIGS['low'];
const VUS = __ENV.VUS ? parseInt(__ENV.VUS, 10) : intensityCfg.vus;
const DURATION = __ENV.DURATION || intensityCfg.duration;
const SLEEP_S = parseFloat(__ENV.SLEEP_S || '1');
const WARMUP_S = parseInt(__ENV.WARMUP_S || '0', 10);

const headers = { 'Content-Type': 'application/json' };

const replicationConfig = readReplicationConfig({
  defaults: {
    timeoutMs: 120000,
    pollIntervalMs: 1000,
    mode: 'strict',
  },
});

// ── Rastreamento interno (para handleSummary) ────────────────────────────────

const summaryRows = {};

function rowKey(entity, action, source, target) {
  return `${entity}|${action}|${source}|${target}`;
}

function trackReplication(entity, action, source, target, success, latencyMs) {
  const key = rowKey(entity, action, source, target);
  if (!summaryRows[key]) {
    summaryRows[key] = {
      entity, action, source, target,
      count: 0, success: 0, failure: 0,
      latencies: [],
    };
  }
  const row = summaryRows[key];
  row.count += 1;
  if (success) {
    row.success += 1;
  } else {
    row.failure += 1;
  }
  row.latencies.push(latencyMs);
}

function percentile(arr, p) {
  if (arr.length === 0) return 0;
  const sorted = arr.slice().sort((a, b) => a - b);
  const idx = Math.ceil((p / 100) * sorted.length) - 1;
  return sorted[Math.max(0, idx)];
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function computeDbLatency(data) {
  if (data && data.replicadoEm && data.criadoEm) {
    const diff = new Date(data.replicadoEm).getTime() - new Date(data.criadoEm).getTime();
    return diff >= 0 ? diff : null;
  }
  return null;
}

function checkPessoaReplication(pessoaId, pessoaNome, targets, sourceName) {
  for (const target of targets) {
    const url = `${BASE_URLS[target]}/pessoas/${pessoaId}`;
    const tags = { entity: 'pessoa', action: 'create', source: sourceName, target };

    const result = waitForReplication({
      url,
      validateFn: (data) => data.id === pessoaId && data.nome === pessoaNome,
      maxAttempts: replicationConfig.maxAttempts,
      intervalMs: replicationConfig.pollIntervalMs,
    });

    if (result.success) {
      const dbLat = computeDbLatency(result.data);
      const latency = dbLat !== null ? dbLat : result.latency;
      replicationLatency.add(latency, tags);
      replicationSuccess.add(1, tags);
      trackReplication('pessoa', 'create', sourceName, target, true, latency);
    } else {
      replicationLatency.add(result.latency, tags);
      replicationFailure.add(1, tags);
      trackReplication('pessoa', 'create', sourceName, target, false, result.latency);
    }

    check(result, {
      [`pessoa replicada ${sourceName}->${target}`]: (r) => r.success,
    });
  }
}

function checkVinculoReplication(pessoaId, sourceName) {
  const url = `${BASE_URLS.diplomas}/vinculos?pessoaId=${pessoaId}`;
  const tags = { entity: 'vinculo', action: 'create', source: sourceName, target: 'diplomas' };

  const result = waitForReplication({
    url,
    validateFn: (data) => Array.isArray(data) && data.some((v) => v.pessoaId === pessoaId),
    maxAttempts: replicationConfig.maxAttempts,
    intervalMs: replicationConfig.pollIntervalMs,
  });

  if (result.success) {
    replicationLatency.add(result.latency, tags);
    replicationSuccess.add(1, tags);
    trackReplication('vinculo', 'create', sourceName, 'diplomas', true, result.latency);
  } else {
    replicationLatency.add(result.latency, tags);
    replicationFailure.add(1, tags);
    trackReplication('vinculo', 'create', sourceName, 'diplomas', false, result.latency);
  }

  check(result, {
    [`vinculo replicado ${sourceName}->diplomas`]: (r) => r.success,
  });

  return result;
}

function concludeAndMeasureStaleness(serviceName, data) {
  if (!data.alunoId) return null;

  const today = isoDate(new Date());
  const baseUrl = serviceName === 'graduacao' ? BASE_URLS.graduacao : BASE_URLS.posGraduacao;

  const updatePayload = serviceName === 'graduacao'
    ? JSON.stringify({
        pessoaId: data.pessoaAluno.id,
        turmaId: data.turmaId,
        dataMatricula: today,
        dataConclusao: today,
        status: 'CONCLUIDO',
      })
    : JSON.stringify({
        pessoaId: data.pessoaAluno.id,
        programaId: data.programaId,
        orientadorId: data.professorId,
        dataMatricula: today,
        dataConclusao: today,
        status: 'CONCLUIDO',
      });

  const updateRes = http.put(`${baseUrl}/alunos/${data.alunoId}`, updatePayload, { headers });

  if (!check(updateRes, { 'aluno concluido': (r) => r.status === 200 })) {
    console.warn(`Falha ao concluir aluno: status=${updateRes.status} body=${updateRes.body}`);
    return null;
  }

  const vinculoResult = waitForReplication({
    url: `${BASE_URLS.diplomas}/vinculos?pessoaId=${data.pessoaAluno.id}`,
    validateFn: (list) => Array.isArray(list) && list.some((v) => v.situacao === 'CONCLUIDO'),
    maxAttempts: replicationConfig.maxAttempts,
    intervalMs: replicationConfig.pollIntervalMs,
  });

  const tags = { entity: 'vinculo', action: 'conclude', source: serviceName, target: 'diplomas' };

  if (vinculoResult.success) {
    // Tenta usar DB timestamps da pessoa como proxy de staleness
    const pessoaRes = http.get(`${BASE_URLS.diplomas}/pessoas/${data.pessoaAluno.id}`);
    let latency = vinculoResult.latency;
    if (pessoaRes.status === 200) {
      try {
        const pessoaData = JSON.parse(pessoaRes.body);
        const dbLat = computeDbLatency(pessoaData);
        if (dbLat !== null) {
          latency = dbLat;
        }
      } catch (_) { /* usa latency de polling */ }
    }

    stalenessWindow.add(latency);
    replicationLatency.add(latency, tags);
    replicationSuccess.add(1, tags);
    trackReplication('vinculo', 'conclude', serviceName, 'diplomas', true, latency);

    return vinculoResult.data.find((v) => v.situacao === 'CONCLUIDO');
  }

  replicationLatency.add(vinculoResult.latency, tags);
  replicationFailure.add(1, tags);
  trackReplication('vinculo', 'conclude', serviceName, 'diplomas', false, vinculoResult.latency);

  check(vinculoResult, {
    [`conclusao replicada ${serviceName}->diplomas`]: (r) => r.success,
  });

  return null;
}

function requestDiploma(pessoaId, vinculo, cursoCodigo, cursoNome, cursoTipo) {
  const today = isoDate(new Date());
  const payload = JSON.stringify({
    pessoaId,
    vinculoId: vinculo.id,
    cursoCodigo,
    cursoNome,
    cursoTipo,
    dataConclusao: vinculo.dataConclusao || today,
    dataSolicitacao: today,
  });

  const res = http.post(`${BASE_URLS.diplomas}/requerimentos`, payload, { headers });

  // 201 = criado com sucesso (C2a3/EDA)
  // 500 com "uq_requerimento_diploma_vinculo" = trigger já criou (C1/C2a1/C2a2)
  const created = res.status === 201;
  const alreadyExists = res.status === 500
    && typeof res.body === 'string'
    && res.body.indexOf('uq_requerimento_diploma_vinculo') >= 0;

  if (check(res, { 'diploma solicitado': () => created || alreadyExists })) {
    businessThroughput.add(1);
  }
}

// ── Options ──────────────────────────────────────────────────────────────────

const scenariosDef = {};

if (WARMUP_S > 0) {
  scenariosDef.warmup = {
    executor: 'constant-vus',
    vus: 1,
    duration: WARMUP_S + 's',
    exec: 'warmupScenario',
    gracefulStop: '30s',
  };
}

scenariosDef.graduacao_load = {
  executor: 'constant-vus',
  vus: Math.ceil(VUS / 2),
  duration: DURATION,
  exec: 'graduacaoScenario',
  startTime: WARMUP_S > 0 ? WARMUP_S + 's' : '0s',
};

scenariosDef.pos_load = {
  executor: 'constant-vus',
  vus: Math.floor(VUS / 2) || 1,
  duration: DURATION,
  exec: 'posScenario',
  startTime: WARMUP_S > 0 ? WARMUP_S + 's' : '0s',
};

export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
  scenarios: scenariosDef,
  thresholds: {
    'http_req_duration': ['p(95)<5000'],
    'checks': ['rate>0.90'],
    'replication_failure_total': ['count<200'],
    'replication_success_total': ['count>0'],
  },
};

// ── Cenários ─────────────────────────────────────────────────────────────────

/**
 * Warm-up: executa fluxos descartáveis para aquecer o pipeline CDC (Debezium).
 * Métricas NÃO são rastreadas — apenas priming do tópico Kafka.
 */
export function warmupScenario() {
  const data = runGraduacaoFlow({
    baseUrl: BASE_URLS.graduacao,
    runId: `warmup_${__VU}_${__ITER}`,
    scenario: 'warmup',
  });

  // Aguarda replicação sem registrar métricas
  waitForReplication({
    url: `${BASE_URLS.diplomas}/pessoas/${data.pessoaAluno.id}`,
    validateFn: (d) => d.id === data.pessoaAluno.id,
    maxAttempts: replicationConfig.maxAttempts,
    intervalMs: replicationConfig.pollIntervalMs,
  });

  sleep(SLEEP_S);
}

export function graduacaoScenario() {
  const meta = { runId: `run_${__VU}_${__ITER}`, scenario: 'graduacao_load' };

  const data = runGraduacaoFlow({ baseUrl: BASE_URLS.graduacao, ...meta });

  // M1: Latência de replicação de Pessoa (com DB timestamps)
  checkPessoaReplication(
    data.pessoaAluno.id,
    data.pessoaAluno.nome,
    ['diplomas', 'assinatura'],
    'graduacao',
  );

  // M1: Latência de replicação de Vínculo (polling)
  checkVinculoReplication(data.pessoaAluno.id, 'graduacao');

  // M5: Staleness — concluir aluno e medir janela de inconsistência
  const vinculo = concludeAndMeasureStaleness('graduacao', data);

  // M2: Throughput — solicitar diploma (fluxo completo)
  if (vinculo) {
    const cursoCodigo = alphaCode(data.seed, 5);
    requestDiploma(
      data.pessoaAluno.id,
      vinculo,
      cursoCodigo,
      `Curso ${cursoCodigo}`,
      'GRADUACAO',
    );
  }

  sleep(SLEEP_S);
}

export function posScenario() {
  const meta = { runId: `run_pos_${__VU}_${__ITER}`, scenario: 'pos_load' };

  const data = runPosGraduacaoFlow({ baseUrl: BASE_URLS.posGraduacao, ...meta });

  // M1: Latência de replicação de Pessoa (com DB timestamps)
  checkPessoaReplication(
    data.pessoaAluno.id,
    data.pessoaAluno.nome,
    ['diplomas', 'assinatura'],
    'pos-graduacao',
  );

  // M1: Latência de replicação de Vínculo (polling)
  checkVinculoReplication(data.pessoaAluno.id, 'pos-graduacao');

  // M5: Staleness — concluir aluno e medir janela de inconsistência
  const vinculo = concludeAndMeasureStaleness('pos-graduacao', data);

  // M2: Throughput — solicitar diploma (fluxo completo)
  if (vinculo) {
    const programaCodigo = alphaCode(data.seed, 5);
    requestDiploma(
      data.pessoaAluno.id,
      vinculo,
      programaCodigo,
      `Programa ${programaCodigo}`,
      'POS_GRADUACAO',
    );
  }

  sleep(SLEEP_S);
}

// ── handleSummary ────────────────────────────────────────────────────────────

export function handleSummary(data) {
  const m = data.metrics;

  const latVals = m['replication_latency_ms'] ? m['replication_latency_ms'].values : {};
  const succTotal = m['replication_success_total'] ? m['replication_success_total'].values.count : 0;
  const failTotal = m['replication_failure_total'] ? m['replication_failure_total'].values.count : 0;
  const bThroughput = m['business_throughput'] ? m['business_throughput'].values.count : 0;
  const stalVals = m['staleness_window_ms'] ? m['staleness_window_ms'].values : {};
  const checksRate = m['checks'] ? m['checks'].values.rate : 0;
  const errorRate = (succTotal + failTotal) > 0
    ? ((failTotal / (succTotal + failTotal)) * 100).toFixed(2)
    : '0.00';

  const lines = [];
  lines.push('');
  lines.push('╔══════════════════════════════════════════════════════════════╗');
  lines.push('║         METRICAS DA PESQUISA (M1-M5) — RESUMO             ║');
  lines.push('╠══════════════════════════════════════════════════════════════╣');
  lines.push(`║ M1 Latencia replicacao                                     ║`);
  lines.push(`║    avg = ${fmt(latVals.avg)} ms                            `);
  lines.push(`║    p95 = ${fmt(latVals['p(95)'])} ms                       `);
  lines.push(`║    p99 = ${fmt(latVals['p(99)'])} ms                       `);
  lines.push(`║ M2 Throughput                                              ║`);
  lines.push(`║    fluxos completos = ${bThroughput}                       `);
  lines.push(`║    replicacoes ok   = ${succTotal}                         `);
  lines.push(`║ M3 Taxa de erro = ${errorRate}%                            `);
  lines.push(`║    falhas = ${failTotal}  |  total = ${succTotal + failTotal}`);
  lines.push(`║ M4 Consistencia (checks rate) = ${(checksRate * 100).toFixed(2)}%`);
  lines.push(`║ M5 Staleness                                               ║`);
  lines.push(`║    avg = ${fmt(stalVals.avg)} ms                           `);
  lines.push(`║    p95 = ${fmt(stalVals['p(95)'])} ms                      `);
  lines.push(`║    p99 = ${fmt(stalVals['p(99)'])} ms                      `);
  lines.push('╚══════════════════════════════════════════════════════════════╝');

  // Tabela de breakdown por entidade/ação/origem/destino
  const keys = Object.keys(summaryRows).sort();
  if (keys.length > 0) {
    lines.push('');
    lines.push('┌────────────┬──────────┬───────────────┬──────────┬───────┬─────┬──────┬─────────┬─────────┬─────────┬─────────┐');
    lines.push('│ Entidade   │ Acao     │ Origem        │ Destino  │ Total │  OK │ Fail │  P50 ms │  P95 ms │  P99 ms │  MAX ms │');
    lines.push('├────────────┼──────────┼───────────────┼──────────┼───────┼─────┼──────┼─────────┼─────────┼─────────┼─────────┤');

    for (const key of keys) {
      const r = summaryRows[key];
      const p50 = percentile(r.latencies, 50);
      const p95 = percentile(r.latencies, 95);
      const p99 = percentile(r.latencies, 99);
      const max = r.latencies.length > 0 ? Math.max(...r.latencies) : 0;
      lines.push(
        `│ ${pad(r.entity, 10)} │ ${pad(r.action, 8)} │ ${pad(r.source, 13)} │ ${pad(r.target, 8)} │ ${pad(String(r.count), 5)} │ ${pad(String(r.success), 3)} │ ${pad(String(r.failure), 4)} │ ${pad(fmt(p50), 7)} │ ${pad(fmt(p95), 7)} │ ${pad(fmt(p99), 7)} │ ${pad(fmt(max), 7)} │`
      );
    }

    lines.push('└────────────┴──────────┴───────────────┴──────────┴───────┴─────┴──────┴─────────┴─────────┴─────────┴─────────┘');
  }

  lines.push('');
  console.log(lines.join('\n'));

  return {};
}

function fmt(val) {
  if (val === undefined || val === null) return '-';
  return Number(val).toFixed(1);
}

function pad(str, len) {
  return String(str).padEnd(len).slice(0, len);
}
