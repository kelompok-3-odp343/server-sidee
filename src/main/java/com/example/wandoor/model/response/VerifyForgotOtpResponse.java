package com.example.wandoor.model.response;

public record VerifyForgotOtpResponse(
        Boolean status,
        String message,
        String verifiedSession
) {
}
