// Test de charge k6 (https://k6.io) — tâches S2-3 et S3-6.
// Lancer :  k6 run tests/perf/historique-et-virements.js
// Ou sans installer k6 :
//   docker run --rm -i --network host grafana/k6 run - < tests/perf/historique-et-virements.js
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE = __ENV.API_URL || 'http://localhost:8081';

// Comptes de démo générés par scripts/generate-data.sql (identifiants fixes)
const COMPTE_A = '8a8bb7cd-343a-a2ad-99b7-d762030857a2'; // compte de démo n°1
const COMPTE_B = '693a9fdd-4c2f-d070-0968-fba0d07ff3c0'; // compte de démo n°2

export const options = {
  scenarios: {
    lecture: {
      executor: 'ramping-vus',
      stages: [
        { duration: '30s', target: 20 },  // montée en charge
        { duration: '1m', target: 50 },   // plateau
        { duration: '15s', target: 0 },   // descente
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<500'],     // 95 % des requêtes en moins de 500 ms
    http_req_failed: ['rate<0.01'],       // moins de 1 % d'erreurs
  },
};

export default function () {
  const res = http.get(`${BASE}/api/accounts/${COMPTE_A}`);
  check(res, { 'statut 200': (r) => r.status === 200 });

  // Tâche S3-6 : décommenter pour envoyer des virements concurrents A → B
  // const v = http.post(`${BASE}/api/transfers`,
  //   JSON.stringify({ fromAccountId: COMPTE_A, toAccountId: COMPTE_B, amount: 1, currency: 'EUR' }),
  //   { headers: { 'Content-Type': 'application/json' } });
  // check(v, { 'virement 201': (r) => r.status === 201 });

  sleep(0.5);
}
