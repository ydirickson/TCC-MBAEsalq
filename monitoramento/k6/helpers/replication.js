import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

export const replicationLatency = new Trend('replication_latency_ms', true);
export const replicationSuccess = new Counter('replication_success_total');
export const replicationFailure = new Counter('replication_failure_total');

function normalizeMode(value) {
  if (!value) {
    return 'strict';
  }
  return String(value).toLowerCase().trim();
}

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value));
}

export function readReplicationConfig({
  envValue,
  envNumber,
  defaults = {},
} = {}) {
  const readValue = envValue || ((name, fallback) => (__ENV[name] !== undefined ? __ENV[name] : fallback));
  const readNumber = envNumber || ((name, fallback) => {
    const raw = readValue(name, undefined);
    if (raw === undefined || raw === '') {
      return fallback;
    }
    const parsed = Number(raw);
    if (Number.isNaN(parsed)) {
      fail(`Valor invalido para ${name}: ${raw}`);
    }
    return parsed;
  });

  const timeoutMs = readNumber('REPLICATION_TIMEOUT_MS', defaults.timeoutMs || 30000);
  const pollIntervalMs = readNumber('POLL_INTERVAL_MS', defaults.pollIntervalMs || 500);
  const mode = normalizeMode(readValue('REPLICATION_MODE', defaults.mode || 'strict'));
  const sampleRate = clamp(readNumber('REPLICATION_SAMPLE_RATE', defaults.sampleRate || 1), 0, 1);

  const validModes = new Set(['off', 'sampled', 'strict']);
  if (!validModes.has(mode)) {
    fail(`REPLICATION_MODE invalido: ${mode}. Use off, sampled ou strict.`);
  }
  if (timeoutMs <= 0) {
    fail('REPLICATION_TIMEOUT_MS deve ser maior que 0.');
  }
  if (pollIntervalMs <= 0) {
    fail('POLL_INTERVAL_MS deve ser maior que 0.');
  }

  return {
    timeoutMs,
    pollIntervalMs,
    maxAttempts: Math.max(1, Math.floor(timeoutMs / pollIntervalMs)),
    mode,
    sampleRate,
  };
}

export function shouldRunReplicationChecks({ mode, sampleRate, seed }) {
  if (mode === 'off') {
    return false;
  }
  if (mode === 'strict') {
    return true;
  }

  if (typeof seed === 'number' && Number.isFinite(seed)) {
    const normalized = Math.abs(seed % 10000) / 10000;
    return normalized < sampleRate;
  }

  return Math.random() < sampleRate;
}

export function waitForReplication({
  url,
  validateFn,
  maxAttempts,
  intervalMs,
  requestParams,
}) {
  const startedAt = Date.now();

  for (let attempt = 1; attempt <= maxAttempts; attempt += 1) {
    const response = http.get(url, requestParams);

    if (response.status === 200) {
      try {
        const data = JSON.parse(response.body);
        if (validateFn(data, response)) {
          return {
            success: true,
            attempts: attempt,
            latency: Date.now() - startedAt,
            data,
          };
        }
      } catch (err) {
        // Conteudo nao-JSON ou validacao indisponivel, segue tentando ate estourar timeout.
      }
    }

    if (attempt < maxAttempts) {
      sleep(intervalMs / 1000);
    }
  }

  return {
    success: false,
    attempts: maxAttempts,
    latency: Date.now() - startedAt,
    data: null,
  };
}

export function registerReplicationOutcome({
  result,
  successLabel,
  dataLabel,
  dataValidator,
  tags,
}) {
  replicationLatency.add(result.latency, tags);

  const checks = {
    [successLabel]: (r) => r.success,
  };
  if (dataLabel && typeof dataValidator === 'function') {
    checks[dataLabel] = (r) => r.data && dataValidator(r.data);
  }

  const passed = check(result, checks);
  if (passed) {
    replicationSuccess.add(1, tags);
  } else {
    replicationFailure.add(1, tags);
  }
  return passed;
}

export function markReplicationFailure(tags) {
  replicationFailure.add(1, tags);
}
