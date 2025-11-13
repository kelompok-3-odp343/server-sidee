import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 100,          // jumlah virtual user
  duration: '30s',  // lama test
};

export default function () {
  const res = http.get('http://34.87.139.149/api/v1/fetch-dashboard'); // endpoint dashboard
  check(res, {
    'status 200': (r) => r.status === 200,
  });
  sleep(1);
}
