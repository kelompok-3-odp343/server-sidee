package com.example.wandoor.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminApproverListRequest {
    @NotBlank
    String roleName;
}
