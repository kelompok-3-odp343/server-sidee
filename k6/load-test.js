import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: 1,
  duration: '10s',
};

export default function () {
  // 1️⃣ LOGIN DULU
  const loginRes = http.post(
    'http://34.87.139.149:30080/api/auth/login',
    JSON.stringify({
      username: 'HEHSJS',  // ganti sesuai usermu
      password: '1029'   // ganti sesuai passwordmu
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  console.log("LOGIN RAW:", loginRes.body);

  let token;
  try {
    token = JSON.parse(loginRes.body).token;
  } catch (e) {
    console.log("Gagal parsing token!");
  }

  console.log("TOKEN:", token);

  // 2️⃣ FETCH DASHBOARD
  const dashRes = http.get('http://34.87.139.149:30080/api/v1/fetch-dashboard', {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  console.log("Dashboard Status:", dashRes.status);
  console.log("Dashboard Body:", dashRes.body);

  sleep(1);
}
