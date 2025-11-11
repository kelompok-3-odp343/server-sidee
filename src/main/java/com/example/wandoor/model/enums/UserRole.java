package com.example.wandoor.model.enums;

import com.example.wandoor.exception.BusinessException;
import org.springframework.http.HttpStatus;

public enum UserRole {
    NASABAH, MAKER, CHECKER, APPROVAL;

    public static UserRole from(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "Role tidak valid: " + role);
        }
    }
}
