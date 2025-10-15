package com.example.wandoor.model.request;

import io.soabase.recordbuilder.core.RecordBuilder;
import jakarta.validation.constraints.NotBlank;

@RecordBuilder
public record LoginRequest(
        @NotBlank
        String username,

        @NotBlank
        String password

) {
}
