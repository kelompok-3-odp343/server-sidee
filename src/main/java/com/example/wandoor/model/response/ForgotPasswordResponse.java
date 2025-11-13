package com.example.wandoor.model.response;

public record ForgotPasswordResponse(
        Boolean status,
        String message,
        String sessionId
) {
}
