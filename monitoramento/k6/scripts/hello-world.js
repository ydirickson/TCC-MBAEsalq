import http from 'k6/http';
import { check, sleep } from 'k6';

// Carrega a lista de alvos (nome + URL) para healthcheck.
const config = JSON.parse(open('../configs/dev.json'));
// Configuracao de execucao basica para um smoke test rapido.
export const options = {
  vus: Number(__ENV.VUS || 1),
  duration: __ENV.DURATION || '5s',
  thresholds: {
    // Falhas devem ser raras e a latencia baixa.
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

// Faz GET sequencial nos /actuator/health de cada servico.
export default function () {
  for (const target of config.targets) {
    const res = http.get(target.url, {
      // Tag simples para facilitar filtro no Prometheus/Grafana.
      tags: { service: target.name },
    });

    // Verifica apenas que o endpoint responde com 2xx.
    check(res, {
      'status is 2xx': (r) => r.status >= 200 && r.status < 300,
    });
  }

  // Pequena pausa para reduzir spam de requisicoes.
  sleep(1);
}
