package com.example.wandoor.model.response;

public record LoginTokenResponse(
        boolean status,
        String message,
        String token
) {
}
