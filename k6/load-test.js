import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 10,          // jumlah virtual user
  duration: '30s',  // lama test
};

export default function () {
  const res = http.get('http://localhost:8080/api/health'); // ubah ke endpoint kamu
  check(res, {
    'status 200': (r) => r.status === 200,
  });
  sleep(1);
}
