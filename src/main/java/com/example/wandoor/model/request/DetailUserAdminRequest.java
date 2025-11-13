package com.example.wandoor.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DetailUserAdminRequest {
    @NotBlank(message = "User ID nasabah wajib diisi")
    private String targetUserId;
}
