import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 50,          // jumlah virtual user
  duration: '30s',  // lama test
};

export default function () {
  const res = http.get('http://127.0.0.1:8081/api/v1/fetch-dashboard'); // endpoint dashboard
  check(res, {
    'status 200': (r) => r.status === 200,
  });
  sleep(1);
}
