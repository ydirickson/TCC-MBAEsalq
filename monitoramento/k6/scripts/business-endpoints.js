import http from 'k6/http';
import { check, sleep, fail } from 'k6';

// Carrega endpoints de negocio e baseUrls por servico.
const config = JSON.parse(open('monitoramento/k6/configs/endpoints.json'));
const scenario = 'business-endpoints';
const runId = __ENV.RUN_ID || `run_${Date.now()}`;

// Configuracao de execucao para chamadas de negocio basicas.
export const options = {
  vus: Number(__ENV.VUS || 1),
  duration: __ENV.DURATION || '10s',
  thresholds: {
    // Alvo de estabilidade e latencia para endpoints de negocio.
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<800'],
  },
  tags: {
    run_id: runId,
    scenario,
  },
};

// Percorre todos os endpoints mapeados e executa GETs simples.
export default function () {
  for (const target of config.targets) {
    const baseUrl = config.baseUrls[target.name];

    if (!baseUrl) {
      // Falha explicita se faltar base URL do servico.
      fail(`Base URL nao configurada para o servico: ${target.name}`);
    }

    for (const endpoint of target.endpoints) {
      const url = `${baseUrl}${endpoint}`;
      const res = http.get(url, {
        // Tags para filtrar por servico e endpoint no Prometheus/Grafana.
        tags: { service: target.name, endpoint },
        headers: { 'X-Run-Id': runId, 'X-Scenario': scenario },
      });

      // Verifica apenas que o endpoint responde com 2xx.
      check(res, {
        'status is 2xx': (r) => r.status >= 200 && r.status < 300,
      });
    }
  }

  // Pequena pausa para reduzir spam de requisicoes.
  sleep(1);
}
