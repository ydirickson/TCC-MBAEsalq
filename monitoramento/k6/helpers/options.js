import { fail } from 'k6';
import { normalizeProfile, normalizeExecutor } from './util.js';

export function buildOptions({ envValue, envNumber, envJson, runId, scenario }) {
  const thresholds = {
    http_req_failed: [envValue('K6_HTTP_REQ_FAILED', 'rate<0.01')],
    http_req_duration: [envValue('K6_HTTP_REQ_DURATION', 'p(95)<1200')],
    checks: [envValue('K6_CHECKS', 'rate>0.95')],
  };
  const base = {
    thresholds,
    tags: {
      run_id: runId,
      scenario,
    },
  };

  const executionModeRaw = envValue('K6_EXECUTION_MODE', 'constant-vus');
  if (!executionModeRaw) {
    fail('Defina K6_EXECUTION_MODE com um executor valido do k6.');
  }
  const executionMode = normalizeExecutor(executionModeRaw);
  const validModes = new Set([
    'constant-vus', // vus fixos por duracao
    'ramping-vus', // rampa de vus por estagios
    'ramping-arrival-rate', // rampa a taxa (iterações/seg) em estagios
    'constant-arrival-rate', // taxa fixa (iterações/seg) por um tempo
  ]);
  if (!validModes.has(executionMode)) {
    fail(`K6_EXECUTION_MODE invalido: ${executionMode}. Use constant-vus, ramping-vus, constant-arrival-rate ou ramping-arrival-rate.`);
  }
  const arrivalModes = new Set([
    'ramping-arrival-rate', // rampa a taxa (iterações/seg) em estagios
    'constant-arrival-rate', // taxa fixa (iterações/seg) por um tempo
  ]);

  if (executionMode && arrivalModes.has(executionMode)) {
    const arrivalProfile = normalizeProfile(envValue('K6_ARRIVAL_PROFILE', 'leve'));
    const arrivalProfilePresets = {
      leve: {
        startRate: 5,
        stages: [
          { duration: '1m', target: 10 },
          { duration: '1m', target: 20 },
          { duration: '1m', target: 0 },
        ],
        rate: 20,
        duration: '3m',
        preAllocatedVUs: 20,
        maxVUs: 80,
      },
      medio: {
        startRate: 10,
        stages: [
          { duration: '1m', target: 20 },
          { duration: '2m', target: 50 },
          { duration: '2m', target: 80 },
          { duration: '1m', target: 0 },
        ],
        rate: 50,
        duration: '4m',
        preAllocatedVUs: 50,
        maxVUs: 150,
      },
      pesado: {
        startRate: 20,
        stages: [
          { duration: '1m', target: 50 },
          { duration: '2m', target: 100 },
          { duration: '2m', target: 200 },
          { duration: '1m', target: 0 },
        ],
        rate: 100,
        duration: '5m',
        preAllocatedVUs: 100,
        maxVUs: 300,
      },
    };

    const arrivalPreset = arrivalProfilePresets[arrivalProfile];
    if (!arrivalPreset) {
      fail(`K6_ARRIVAL_PROFILE invalido: ${arrivalProfile}. Use leve, medio ou pesado.`);
    }

    const scenarioConfig = {
      executor: executionMode,
      timeUnit: envValue('K6_ARRIVAL_TIME_UNIT', arrivalPreset ? arrivalPreset.timeUnit || '1s' : '1s'),
      preAllocatedVUs: envNumber('K6_ARRIVAL_PREALLOCATED_VUS',
        arrivalPreset ? arrivalPreset.preAllocatedVUs : 50),
      maxVUs: envNumber('K6_ARRIVAL_MAX_VUS',
        arrivalPreset ? arrivalPreset.maxVUs : 200),
    };

    if (executionMode === 'constant-arrival-rate') {
      scenarioConfig.rate = envNumber('K6_ARRIVAL_RATE',
        arrivalPreset ? arrivalPreset.rate : 20);
      scenarioConfig.duration = envValue('K6_ARRIVAL_DURATION',
        arrivalPreset ? arrivalPreset.duration : '3m');
    } else {
      // ramping-arrival-rate: adiciona fallbacks para evitar falhas se propriedades estiverem ausentes
      scenarioConfig.startRate = envNumber('K6_ARRIVAL_START_RATE',
        arrivalPreset && typeof arrivalPreset.startRate === 'number'
          ? arrivalPreset.startRate
          : 5);
      scenarioConfig.stages = envJson('K6_ARRIVAL_STAGES',
        arrivalPreset && Array.isArray(arrivalPreset.stages)
          ? arrivalPreset.stages
          : [{ duration: '1m', target: 10 }, { duration: '1m', target: 0 }]);
    }

    return {
      ...base,
      scenarios: {
        [scenario]: scenarioConfig,
      },
    };
  }

  if (executionMode === 'constant-vus') {
    return {
      ...base,
      vus: envNumber('VUS', envNumber('K6_VUS', 1)),
      duration: envValue('DURATION', envValue('K6_DURATION', '30s')),
    };
  }

  const profile = normalizeProfile(envValue('TEST_PROFILE', 'leve'));
  if (executionMode === 'ramping-vus' && !profile) {
    fail('Defina TEST_PROFILE (leve/medio/pesado) para usar ramping-vus.');
  }
  const defaultStages = {
    leve: [
      { duration: '20s', target: 2 },
      { duration: '40s', target: 2 },
      { duration: '10s', target: 0 },
    ],
    medio: [
      { duration: '20s', target: 5 },
      { duration: '40s', target: 10 },
      { duration: '40s', target: 10 },
      { duration: '20s', target: 0 },
    ],
    pesado: [
      { duration: '30s', target: 10 },
      { duration: '60s', target: 25 },
      { duration: '60s', target: 50 },
      { duration: '30s', target: 0 },
    ],
  };
  const stageProfiles = {
    leve: envJson('K6_PROFILE_LEVE_STAGES', defaultStages.leve),
    medio: envJson('K6_PROFILE_MEDIO_STAGES', defaultStages.medio),
    pesado: envJson('K6_PROFILE_PESADO_STAGES', defaultStages.pesado),
  };

  if (profile) {
    const stages = stageProfiles[profile];
    if (!stages) {
      fail(`TEST_PROFILE invalido: ${profile}. Use leve, medio ou pesado.`);
    }
    return {
      ...base,
      stages,
    };
  }

  return {
    ...base,
    vus: envNumber('VUS', envNumber('K6_VUS', 1)),
    duration: envValue('DURATION', envValue('K6_DURATION', '30s')),
  };
}
