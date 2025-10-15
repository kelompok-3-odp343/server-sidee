package com.example.wandoor.model.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
        @NotBlank
        String otp_ref,

        @NotBlank
        String otp_code
) {
}
