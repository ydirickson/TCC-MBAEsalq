import http from 'k6/http';
import { check, sleep } from 'k6';

const config = JSON.parse(open('../configs/dev.json'));
const runId = __ENV.RUN_ID || `run_${Date.now()}`;

export const options = {
  vus: Number(__ENV.VUS || 1),
  duration: __ENV.DURATION || '5s',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
  tags: {
    run_id: runId,
    scenario: 'hello-world',
  },
};

export default function () {
  for (const target of config.targets) {
    const res = http.get(target.url, {
      tags: { service: target.name },
      headers: { 'X-Run-Id': runId },
    });

    check(res, {
      'status is 2xx': (r) => r.status >= 200 && r.status < 300,
    });
  }

  sleep(1);
}
