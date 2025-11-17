package com.example.wandoor.service;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.exception.BusinessException;
import com.example.wandoor.model.request.AdminApproverListRequest;
import com.example.wandoor.model.response.AdminApproverDataResponse;
import com.example.wandoor.model.response.GenericResponse;
import com.example.wandoor.repository.AdminProfileRepository;
import com.example.wandoor.repository.RoleManagementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class AdminApproverListService {


    private final RoleManagementRepository roleManagementRepository;
    private final AdminProfileRepository adminProfileRepository;

    public GenericResponse<List<AdminApproverDataResponse>> getApproverList(AdminApproverListRequest request){
        var adminUserId = RequestContext.get().getUserId();
        adminProfileRepository.findById(adminUserId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.CONFLICT,
                        "NO_SUCH_ADMIN",
                        "No Such Admin"
                ));

        var roleData = roleManagementRepository.findByRoleName(request.getRoleName())
                .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "INVALID_ROLE_NAME", "Invalid Role Name"));
        var approverList = adminProfileRepository.findByRoleId(roleData.getId());

        List<AdminApproverDataResponse> responses = approverList.stream()
                .map(item -> AdminApproverDataResponse.builder()
                        .userId(item.getId())
                        .npp(item.getNpp())
                        .fullName(item.getFullName())
                        .displayName(item.getNpp() + " - " + item.getFullName())
                        .roleId(item.getRoleId())
                        .roleName(roleData.getRoleName())
                        .build())
                .toList();

        return GenericResponse.<List<AdminApproverDataResponse>>builder()
                .data(responses)
                .build();
    }
}
