package com.example.wandoor.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.entity.UserAuth;
import com.example.wandoor.model.enums.UserRole;
import com.example.wandoor.model.request.*;
import com.example.wandoor.model.response.*;
import com.example.wandoor.repository.*;
import com.example.wandoor.util.OtpGuards;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.wandoor.model.entity.RoleManagement;
import com.example.wandoor.util.Helpers;
import com.example.wandoor.util.JwtUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoginOtpService {
    private final UserAuthRepository userAuthRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(5);
    private final UserOtpVerificationRepository userOtpVerificationRepository;
    private final EmailService emailService;
    private final Helpers helpers;
    private final ProfileRepository profileRepository;
    private final RoleManagementRepository roleManagementRepository;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;
    private final BlockUserNow blockUserNow;
    private final AdminProfileRepository adminProfileRepository;

    private static final Duration OTP_TTL = Duration.ofMinutes(3);
    private static final Duration BLOCK_TTL = Duration.ofMinutes(10);
    private static final Duration LOGIN_FAIL_TTL = Duration.ofHours(1);
    private static final Duration TOKEN_TTL = Duration.ofHours(2);
    private static final Duration VERIFY_TTL = Duration.ofMinutes(10);

    private static final int MAX_LOGIN_FAIL = 3;
    private static final int MAX_OTP_FAIL = 3;


    @Transactional
    public LoginResponse login(LoginRequest req) {
        var username = req.username();
        if (username == null || username.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Username harus diisi");
        }

        try {
            var userAuth = userAuthRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Username atau Password salah"));


            if (userAuth.getIsUserBlocked() != null && Integer.valueOf(1).equals(userAuth.getIsUserBlocked())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_BLOCKED", "Akun diblokir, hubungi CS untuk membuka blokir.");
            }

            var temporaryBlockAccount = "otp:blocked:user:" + req.username();
            if (stringRedisTemplate.hasKey(temporaryBlockAccount)) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_TEMP_BLOCKED", "akun diblokir sementara, tunggu beberapa saat");
            }

            var checkPassword = passwordEncoder.matches(req.password(), userAuth.getPassword());
            if (!checkPassword) {
                var failKey = "otp:login:failed:" + req.username();
                Long failCount = stringRedisTemplate.opsForValue().increment(failKey);
                if (failCount == 1) stringRedisTemplate.expire(failKey, LOGIN_FAIL_TTL);
                if (failCount >= MAX_LOGIN_FAIL) {
                    blockUserNow.blockUserNow(userAuth.getUserId());
                    throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_BLOCKED","Akun diblokir karena 3 kali gagal login. Hubungi CS.");
                }
                throw new BusinessException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Username atau Password salah");
            }

            var role = roleManagementRepository.findById(userAuth.getRoleId())
                    .map(RoleManagement::getRoleName)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", "role not found"));

            var roleEnum = UserRole.from(role);


            switch (roleEnum) {
                case NASABAH -> { return doNasabahLogin(userAuth); }
                case MAKER, CHECKER, APPROVAL -> {
                    var adminProfile = adminProfileRepository.findById(userAuth.getUserId())
                            .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "NO_SUCH_ADMIN", "No Such Admin"));
                    Map<String, Object> claims = new HashMap<>();
                    claims.put("role", roleEnum.name());
                    claims.put("username", userAuth.getUsername());
                    claims.put("email", userAuth.getEmailAddress());
                    claims.put("npp", adminProfile.getNpp());
                    String token = jwtUtils.generateToken(claims, userAuth.getUserId());
                    stringRedisTemplate.opsForValue().set("session:admin:" + userAuth.getUserId(), token, TOKEN_TTL);
                    return new LoginResponse(true, "Login berhasil sebagai " + roleEnum.name(),  token);
                }
                default -> throw new BusinessException(HttpStatus.FORBIDDEN, "ROLE_NOT_ALLOWED", "Role tidak diizinkan login");
            }


        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while login", e);
        }

    }

    @Transactional
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest req) {
        try {
            var otpSessionKey = "otp:session:" + req.sessionId();
            Map<Object, Object> otpData = stringRedisTemplate.opsForHash().entries(otpSessionKey);

            if (otpData.isEmpty())
                throw new BusinessException(HttpStatus.BAD_REQUEST, "OTP_NOT_FOUND", "OTP expired atau tidak ditemukan");

            var username = (String) otpData.get("username");
            var storedOtp = (String) otpData.get("otp");
            var userId = (String) otpData.get("userId");

            var verifyAttemptKey = "otp:verify_attempt:" + req.sessionId();
            Long attemptCount = 0L;

            // counter attempt
            if (!req.otpCode().equals(storedOtp)) {
                attemptCount = stringRedisTemplate.opsForValue().increment(verifyAttemptKey);
            if (attemptCount == 1) stringRedisTemplate.expire(verifyAttemptKey, OTP_TTL);
                log.warn("OTP salah (attempt ke {}) untuk user {}", attemptCount, username);

                if (attemptCount >= MAX_OTP_FAIL) {
                    stringRedisTemplate.opsForValue().set("otp:blocked:user:" + username, "true", BLOCK_TTL);
                    log.warn("User {} diblokir sementara selama {} menit", username, BLOCK_TTL.toMinutes());
                    return new VerifyOtpResponse(
                            false,
                            "Terlalu banyak percobaan OTP. Akun diblokir sementara.",
                            null,
                            null,
                            attemptCount.intValue()
                    );
                }

                return new VerifyOtpResponse(
                        false,
                        "OTP salah. Percobaan ke-" + attemptCount + " dari " + MAX_OTP_FAIL + ".",
                        null,
                        null,
                        attemptCount.intValue()
                );
            }

            stringRedisTemplate.delete(otpSessionKey);
            stringRedisTemplate.delete("otp:verify_attempt:" + req.sessionId());

            var userData = userAuthRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND" , "user not found"));
            var role = roleManagementRepository.findById(userData.getRoleId())
                    .map(RoleManagement::getRoleName)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "ROLE_NOT_FOUND", "Role tidak ditemukan"));
            var profile = profileRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND" , "profile not found"));

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", role);
            claims.put("username", userData.getUsername());
            claims.put("cif", profile.getCif());
            var token = jwtUtils.generateToken(claims, userData.getUserId());

            var sessionKey = "session:" + req.sessionId();
            stringRedisTemplate.opsForValue().set(sessionKey, token, TOKEN_TTL);

            var dataUser = new VerifyOtpResponse.User(
                    userData.getUserId(),
                    profile.getCif(),
                    userData.getUsername(),
                    role
            );

            return new VerifyOtpResponse(true, "login berhasil", token, dataUser, attemptCount.intValue());

        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while verify OTP", e);
        }
    }

    @Transactional
    public ResendOtpResponse resendOtp(ResendOtpRequest req) {
        try {
            var sessionKey = "otp:session:" + req.sessionId();
            var sessionData = stringRedisTemplate.opsForHash().entries(sessionKey);

            if (sessionData.isEmpty())
                throw new BusinessException(HttpStatus.BAD_REQUEST, "SESSION_NOT_FOUND" , "Session tidak ditemukan atau OTP expired, silahkan login ulang");

            var username = (String) sessionData.get("username");
            var email = (String) sessionData.get("email");

            if (stringRedisTemplate.hasKey("otp:cooldown:user:" + username)) {
                long remaining = stringRedisTemplate.getExpire("otp:cooldown:user:" + username, TimeUnit.SECONDS);
                throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUEST" ,
                        "Tunggu " + remaining + " detik sebelum mengirim OTP lagi.");
            }

            var resendKey = "otp:resend:user:" + username;
            Long resendCount = stringRedisTemplate.opsForValue().increment(resendKey);
            if (resendCount == 1) stringRedisTemplate.expire(resendKey, Duration.ofMinutes(10));

            log.info("User {} melakukan resend OTP ke-{} dari 3", username, resendCount);

            if (resendCount >= 3) {
                stringRedisTemplate.opsForValue().set("otp:cooldown:user:" + username, "true", Duration.ofSeconds(120));
                stringRedisTemplate.opsForValue().set("otp:blocked:user:" + username, "true", Duration.ofMinutes(10));

                var finalOtp = OtpGuards.generateNumericOtp();
                stringRedisTemplate.opsForHash().put(sessionKey, "otp", finalOtp);
                stringRedisTemplate.expire(sessionKey, Duration.ofMinutes(3));
                emailService.sendOtp(email, finalOtp);

                log.warn("User {} sudah 3x resend OTP, diblokir sementara & cooldown aktif 120s", username);

                return new ResendOtpResponse(
                        true,
                        "Kode OTP baru telah dikirim. Anda telah mencapai batas resend maksimal. Tunggu 2 menit sebelum mencoba lagi.",
                        120
                );
            }

                var newOtp = OtpGuards.generateNumericOtp();
                stringRedisTemplate.opsForHash().put(sessionKey, "otp", newOtp);
                stringRedisTemplate.expire(sessionKey, Duration.ofMinutes(3));

                emailService.sendOtp(email, newOtp);

                return new ResendOtpResponse(
                        true,
                        "Kode OTP baru telah dikirim ke email Anda. Resend ke-" + resendCount + " dari 3.",
                        0
                );

        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while resend OTP", e);
        }
    }

    @Transactional
    public LogoutResponse logout(String token, String userId) {
        try {
            var sessionKey = "session:" + userId;
            stringRedisTemplate.delete(sessionKey);

            var blacklistKey = "jwt_blacklist:" + token;
            stringRedisTemplate.opsForValue().set(blacklistKey, "true");

            long ttlSeconds = jwtUtils.getRemainingValidity(token);
            stringRedisTemplate.expire(blacklistKey, Duration.ofSeconds(ttlSeconds));

            return new LogoutResponse(true, "logout berhasil");

        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while logout", e);
        }
    }

    public ForgotPasswordResponse requestOtp(ForgotPasswordRequest req){
        try {
            var user = userAuthRepository.findByUsername(req.username())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "INVALID_USER" ,"user not found"));

            var temporaryBlockKey = "otp:blocked:user:" + user.getUsername();
            if (stringRedisTemplate.hasKey(temporaryBlockKey)) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "ACCOUNT_BLOCKED" ,"Akun sedang diblokir sementara, silakan coba lagi nanti.");
            }

            var sessionId = UUID.randomUUID().toString();
            var otp = OtpGuards.generateNumericOtp();

            var keyForgotOtp = "otp:forgot:" + sessionId;
            stringRedisTemplate.opsForHash().putAll(keyForgotOtp, Map.of(
                    "username", user.getUsername(),
                    "email", user.getEmailAddress(),
                    "otp", otp
            ));

            stringRedisTemplate.expire(keyForgotOtp, OTP_TTL);

            emailService.sendOtp(user.getEmailAddress(), otp);
            return new ForgotPasswordResponse(
                    true,
                    "Kode OTP telah dikirim ke email Anda.",
                    sessionId
            );
        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while request OTP", e);
        }
    }

    public VerifyForgotOtpResponse verifyForgotOtp(VerifyOtpRequest req){
        try {
            var otpData = stringRedisTemplate.opsForHash().entries("otp:forgot:" + req.sessionId());
            if (otpData == null || otpData.isEmpty()){
                throw new BusinessException(HttpStatus.GONE, "OTP_EXPIRED" ,"OTP sudah kadaluarsa atau tidak ditemukan");
            }

            var username = (String) otpData.get("username");
            var otp = (String) otpData.get("otp");

            if (!req.otpCode().equals(otp)) {
                var verifyAttemptKey = "otp:forgot:fail:" + username;
                Long attemptCount = stringRedisTemplate.opsForValue().increment(verifyAttemptKey);
                if (attemptCount == 1) stringRedisTemplate.expire(verifyAttemptKey, OTP_TTL);

                log.warn("OTP salah (attempt ke {}) untuk user {}", attemptCount, username);

                if (attemptCount >= MAX_OTP_FAIL) {
                    stringRedisTemplate.opsForValue().set("otp:blocked:user:" + username, "true", BLOCK_TTL);
                    log.warn("User {} diblokir sementara selama {} menit", username, BLOCK_TTL.toMinutes());
                    return new VerifyForgotOtpResponse(
                            false,
                            "Terlalu banyak percobaan OTP, akun di blokir sementara",
                            null
                    );
                }

                return new VerifyForgotOtpResponse(
                        false,
                        "Otp salah. percobaan ke-" + attemptCount + "dari  " + MAX_OTP_FAIL,
                        null
                );

            }
            var verifiedKey = "otp:forgot:verified:" + username;
            stringRedisTemplate.opsForValue().set(verifiedKey, "true", VERIFY_TTL);
            stringRedisTemplate.delete("otp:forgot:" + req.sessionId());
            return new VerifyForgotOtpResponse(true, "Verifikasi OTP berhasil. Silakan buat password baru.", verifiedKey);

        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while verify OTP", e);
        }
    }

    public BaseResponse resetPassword(ResetPasswordRequest req){
        try{
            var isVerified = stringRedisTemplate.hasKey(req.verifiedSession());
            if (!isVerified) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session verifikasi tidak valid atau kadaluarsa");

            if(!req.newPassword().equals(req.confirmPassword())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "konfirmasi password tidak cocok");

            if (!req.newPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$"))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password tidak memenuhi kriteria");

            var username = req.verifiedSession().replace("otp:forgot:verified:", "");
            var user = userAuthRepository.findByUsername(username)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

            user.setPassword(passwordEncoder.encode(req.newPassword()));
            userAuthRepository.save(user);

            stringRedisTemplate.delete(req.verifiedSession());
            stringRedisTemplate.delete("otp:forgot:fail:" + username);
            return new BaseResponse(true, "Password berhasil diperbarui. Silakan login kembali.");
        } catch (BusinessException e) {
            throw e;
        }  catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while reset password", e);
        }

    }

    private LoginResponse doNasabahLogin(UserAuth userAuth){
        try{
            stringRedisTemplate.delete("otp:login_failed:" + userAuth.getUsername());

            var sessionId = UUID.randomUUID().toString();
            var otp = OtpGuards.generateNumericOtp();

            var otpSessionKey = "otp:session:" + sessionId;
            Map<String, String> otpData = Map.of(
                    "userId", userAuth.getUserId(),
                    "username", userAuth.getUsername(),
                    "email", userAuth.getEmailAddress(),
                    "otp", otp
            );
            stringRedisTemplate.opsForHash().putAll(otpSessionKey, otpData);
            stringRedisTemplate.expire(otpSessionKey, OTP_TTL);


            var profile = profileRepository.findById(userAuth.getUserId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "PROFILE_NOT_FOUND", "Profile tidak ditemukan"));
            var emailTo = userAuth.getEmailAddress() != null
                    ? userAuth.getEmailAddress()
                    : profile.getEmailAddress();

            // sent OTP by email
            emailService.sendOtp(emailTo, otp);

            log.info("OTP {} dikirim ke {} | session={} TTL={}m", otp, userAuth.getEmailAddress(), sessionId, OTP_TTL.toMinutes());

            return new LoginResponse(true, "Kode OTP telah dikirim ke email Anda", sessionId);
        } catch (BusinessException e){
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR", "Something went wrong while login", e);
        }
    }


}




