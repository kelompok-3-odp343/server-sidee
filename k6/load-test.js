import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 7,          // jumlah virtual user
  duration: '30s',  // lama test
};

export default function () {
  const res = http.get('http://localhost:8000/api/v1/fetch-dashboard'); // ubah ke endpoint kamu
  check(res, {
    'status 200': (r) => r.status === 200,
  });
  sleep(1);
}
