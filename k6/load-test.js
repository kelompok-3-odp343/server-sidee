import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
  vus: 100,
  duration: '100s',
};

export default function () {
  // Step 1: Login untuk trigger OTP
  const loginRes = http.post(
    'http://34.87.139.149:30080/api/auth/login',
    JSON.stringify({
      username: 'P006',  // akun test
      password: '123456' // password test
    }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  let loginJson;
  try {
    loginJson = JSON.parse(loginRes.body);
  } catch {
    console.error("Login response bukan JSON:", loginRes.body);
    return;
  }

  // Ambil sessionId / otpId dari response login
  const sessionId = loginJson.sessionIdOrToken || loginJson.otpId;
  if (!sessionId) {
    console.error("Login gagal atau OTP tidak dikirim:", loginRes.body);
    return;
  }
  console.log("OTP ID / sessionId:", sessionId);

  // Step 2: Verifikasi OTP (pakai OTP default test env)
  // const otpRes = http.post('http://34.87.139.149:30080/api/auth/verify-otp',
  //   JSON.stringify({
  //     sessionId: "f5ffca9b-ef09-4a8a-88a2-d621d91d83e3", 
  //     otpCode: "637647" // OTP default di test environment
  //   }),
  //   { headers: { 'Content-Type': 'application/json' } }
  // );

  // let otpJson;
  // try {
  //   otpJson = JSON.parse(otpRes.body);
  // } catch {
  //   console.error("OTP response bukan JSON:", otpRes.body);
  //   return;
  // }

  // const token = otpJson.sessionIdOrToken;
  // if (!token) {
  //   console.error("Verifikasi OTP gagal:", otpRes.body);
  //   return;
  // }
  // console.log("Berhasil OTP → Token:", token);

  // // Step 3: Hit dashboard pakai token
  // const dashRes = http.get(
  //   'http://34.87.139.149:30080/api/v1/fetch-dashboard',
  //   { headers: { Authorization: `Bearer ${token}` } }
  // );

  // if (dashRes.status !== 200) {
  //   console.error("Dashboard error:", dashRes.status, dashRes.body);
  // } else {
  //   console.log("Dashboard berhasil:", dashRes.body);
  // }

  sleep(1);
}
