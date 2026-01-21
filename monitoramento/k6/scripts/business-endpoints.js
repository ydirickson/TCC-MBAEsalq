import http from 'k6/http';
import { check, sleep, fail } from 'k6';

const config = JSON.parse(open('../configs/business.json'));
const runId = __ENV.RUN_ID || `run_${Date.now()}`;

export const options = {
  vus: Number(__ENV.VUS || 1),
  duration: __ENV.DURATION || '10s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<800'],
  },
  tags: {
    run_id: runId,
    scenario: 'business-endpoints',
  },
};

export default function () {
  for (const target of config.targets) {
    const baseUrl = config.baseUrls[target.name];

    if (!baseUrl) {
      fail(`Base URL nao configurada para o servico: ${target.name}`);
    }

    for (const endpoint of target.endpoints) {
      const url = `${baseUrl}${endpoint}`;
      const res = http.get(url, {
        tags: { service: target.name, endpoint },
        headers: { 'X-Run-Id': runId },
      });

      check(res, {
        'status is 2xx': (r) => r.status >= 200 && r.status < 300,
      });
    }
  }

  sleep(1);
}
