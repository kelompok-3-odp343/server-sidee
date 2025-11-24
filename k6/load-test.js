import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: 1000,
  duration: '10s',
};

export default function () {
  // Step 1: Login untuk trigger OTP
  // const loginRes = http.post(
  //   'https://api.wandoor.my.id/api/auth/login',
  //   JSON.stringify({
  //     username: 'P004',  // akun test
  //     password: '123456' // password test
  //   }),
  //   { headers: { 'Content-Type': 'application/json' } }
  // );

  // let loginJson;
  // try {
  //   loginJson = JSON.parse(loginRes.body);
  // } catch {
  //   console.error("Login response bukan JSON:", loginRes.body);
  //   return;
  // }

  // // Ambil sessionId / otpId dari response login
  // const sessionId = loginJson.sessionIdOrToken || loginJson.otpId;
  // if (!sessionId) {
  //   console.error("Login gagal atau OTP tidak dikirim:", loginRes.body);
  //   return;
  // }
  // console.log("OTP ID / sessionId:", sessionId);


  // Step 3: Hit dashboard pakai token
  const token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJQMDAzIiwicm9sZSI6IkNIRUNLRVIiLCJlbWFpbCI6Im9rdGF2aWFxLmFAZ21haWwuY29tIiwibnBwIjoiNjQ4ODkiLCJpYXQiOjE3NjM1MjQ2OTUsImV4cCI6MTc2MzUyODI5NX0.zRb6SUuu45e1GyDlGjDOyhVWbhNgJq_ElYt_SpjKa44";

  const dashRes = http.get(
  'http://api.wandoor.my.id/api/admin/user/list',
  { headers: { Authorization: `Bearer ${token}` } }
  );


  sleep(1);
}
