package com.example.wandoor.model.response;


import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record LoginResponse(
        boolean status,
        String message,
        String role,
        String sessionIdOrToken
) {
}
