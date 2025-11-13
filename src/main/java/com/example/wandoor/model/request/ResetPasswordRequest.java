package com.example.wandoor.model.request;

public record ResetPasswordRequest(
        String verifiedSession,
        String newPassword,
        String confirmPassword
) {
}
