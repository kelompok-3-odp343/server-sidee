package com.example.wandoor.model.response;

public record VerifyOtpResponse(
        boolean status,
        String message,
        String token,
        Integer attemptCount
) {
}
