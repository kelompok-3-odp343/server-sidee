import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 1,          // jumlah virtual user
  duration: '10s',  // lama test
};

export default function () {
  const res = http.get('http://34.87.139.149/api/v1/fetch-dashboard'); // endpoint dashboard

  console.log(`Status: ${res.status}`);
  console.log(`Body: ${res.body.substring(0, 200)}`); // lihat isi respons pertama 200 char
  
  check(res, {
    'status 200': (r) => r.status === 200,
  });
  sleep(1);
}
