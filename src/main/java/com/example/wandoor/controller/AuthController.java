package com.example.wandoor.controller;

import com.example.wandoor.model.request.*;
import com.example.wandoor.model.response.*;
import com.example.wandoor.service.LoginOtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final LoginOtpService loginOtpService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (
            @Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(loginOtpService.login(req));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<VerifyOtpResponse> verifyOtp (
            @Valid @RequestBody VerifyOtpRequest req){
        return ResponseEntity.ok(loginOtpService.verifyOtp(req));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@Valid @RequestBody ResendOtpRequest req) {
        return ResponseEntity.ok(loginOtpService.resendOtp(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponse> logout (
            @RequestHeader("Authorization") String auth,
            @RequestHeader("User-Id") String userId
    ){
        String token = auth.replace("Bearer ", "");
        return ResponseEntity.ok(loginOtpService.logout(token, userId));
    }

    @PostMapping("forgot-password/request-otp")
    public ResponseEntity<ForgotPasswordResponse> requestOtp (
            @Valid @RequestBody ForgotPasswordRequest request
            ){
        return ResponseEntity.ok(loginOtpService.requestOtp(request));
    }

    @PostMapping("forgot-password/verify-otp")
    public ResponseEntity<VerifyForgotOtpResponse> verifyForgotOtp(
            @Valid @RequestBody VerifyOtpRequest req ) {
                return ResponseEntity.ok(loginOtpService.verifyForgotOtp(req));
    }

    @PostMapping("forgot-password/reset-password")
    public ResponseEntity<BaseResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest req
            ){
        return ResponseEntity.ok(loginOtpService.resetPassword(req));
    }




}
