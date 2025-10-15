package com.example.wandoor.service;

import com.example.wandoor.model.entity.OtpVerification;
import com.example.wandoor.model.entity.RoleManagement;
import com.example.wandoor.model.entity.UserAuth;
import com.example.wandoor.model.request.LoginRequest;
import com.example.wandoor.model.request.VerifyOtpRequest;
import com.example.wandoor.model.response.LoginResponse;
import com.example.wandoor.model.response.VerifyOtpResponse;
import com.example.wandoor.repository.ProfileRepository;
import com.example.wandoor.repository.RoleManagementRepository;
import com.example.wandoor.repository.UserAuthRepository;
import com.example.wandoor.repository.UserOtpVerificationRepository;
import com.example.wandoor.util.Helpers;
import com.example.wandoor.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoginOtpService{
    private final UserAuthRepository userAuthRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserOtpVerificationRepository userOtpVerificationRepository;
    private final EmailService emailService;
    private final Helpers helpers;
    private final ProfileRepository profileRepository;
    private final RoleManagementRepository roleManagementRepository;
    private final JwtUtils jwtUtils;

    private static final Duration OTP_EXPIRED = Duration.ofMinutes(3);
    private static final Duration ISSUE_WINDOW = Duration.ofMinutes(10);
    private static final int ISSUE_LIMIT = 3;


    @Transactional
    public LoginResponse login (LoginRequest req){
        // any userId in db?
        var userAuth = userAuthRepository.findByUsername(req.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "UserId atau password salah"));

        // cek apakah userAuth di block
        if (userAuth.getIsUserBlocked() != null && userAuth.getIsUserBlocked() == 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Akun diblokir, hubungi CS.");
        }

        //  password verification
        var checkPassword = passwordEncoder.matches(req.password(), userAuth.getPassword());
        if (!checkPassword) {
            return new LoginResponse(false, "Invalid Credential", null);
        }

        // anti spam email per user


        // get email form profile
        var profile = profileRepository.findById(userAuth.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Profile tidak ditemukan"));
//        var emailTo = userAuth.getEmailAddress() != null ? userAuth.getEmailAddress() ? profile.getEmailAddress();

        // generate OTP
        var otp = String.format("%06d", new Random().nextInt(999999));
        var otpReff = UUID.randomUUID().toString();

        // save to table OtpVerification
        var otpVerification = OtpVerification.builder()
                .id(otpReff)
                .userId(userAuth.getUserId())
                .otpCode(otp)
                .emailTo(userAuth.getEmailAddress())
                .expiresAt(LocalDateTime.now().plusMinutes(3))
                .isUsed(0)
                .createdTime(LocalDateTime.now())
                .build();
        userOtpVerificationRepository.save(otpVerification);

        // sent OTP by email
        emailService.sendOtp(userAuth.getEmailAddress(), otp);

        return new LoginResponse(true, "Kode OTP telah dikirim ke email Anda", otpReff);
    }

    public VerifyOtpResponse verifyOtp(VerifyOtpRequest req) {
        var ref = UUID.fromString(req.otp_ref());
        // get row otp
        var otpRow = userOtpVerificationRepository.findByIdAndIsUsed(req.otp_ref(), 0)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP tidak valid"));

        // cek expired
        if (otpRow.getExpiresAt().isBefore(LocalDateTime.now())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired");
        }

        // compare otp input and db
        if (!helpers.safeEquals(otpRow.getOtpCode(), req.otp_code())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "OTP Salah");
        }

        // used and make jwt
        otpRow.setIsUsed(1);
        userOtpVerificationRepository.save(otpRow);

        var role = roleManagementRepository.findFirstByUserId(otpRow.getUserId())
                        .map(RoleManagement::getRoleName).orElse("Nasabah");
        var userData = userAuthRepository.findById(otpRow.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "user not found"));

        var token = jwtUtils.generateToken(otpRow.getUserId(), role);

        var dataUser = new VerifyOtpResponse.User(
                userData.getUserId(),
                userData.getUsername(),
                role
        );

        return new VerifyOtpResponse(true, "login berhasil", token, dataUser);
    }
}

