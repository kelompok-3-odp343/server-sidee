package com.example.wandoor.model.response;

import com.example.wandoor.model.entity.AdminProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminApproverDataResponse {
    private String userId;
    private String npp;
    private String fullName;
    private String displayName;
    private String roleId;
    private String roleName;
}
