package com.example.wandoor.controller;

import com.example.wandoor.model.entity.TrActivity;
import com.example.wandoor.model.request.AdminActivityDetailRequest;
import com.example.wandoor.model.request.AdminApproverListRequest;
import com.example.wandoor.model.request.AdminBlockUnblockUserRequest;
import com.example.wandoor.model.request.DetailUserAdminRequest;
import com.example.wandoor.model.response.*;
import com.example.wandoor.service.AdminApproverListService;
import com.example.wandoor.service.AdminMenuAccessService;
import com.example.wandoor.service.AdminService;
import com.example.wandoor.service.DetailUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DetailUserAdminService detailUserAdminService;
    private final AdminMenuAccessService adminMenuAccessService;
    private final AdminApproverListService adminApproverListService;
    private final AdminService adminService;

    @PostMapping("/detail-user")
    public ResponseEntity<DetailUserAdminResponse> getDetail(@RequestBody DetailUserAdminRequest request) {
        DetailUserAdminResponse response = detailUserAdminService.getUserDetail(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/menu-access")
    public ResponseEntity<Map<String, Map<String, String>>> getMenu(){
        Map<String, Map<String, String>> response = adminMenuAccessService.getMenuStructure();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approver-list")
    public ResponseEntity<GenericResponse<List<AdminApproverDataResponse>>> getApproverList(
            @RequestBody AdminApproverListRequest request
            ) {
        var response = adminApproverListService.getApproverList(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/activity/list")
    public ResponseEntity<ActivityListResponse> getActivityList() {
        var response = adminService.getAllActivityPerAdmin();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/activity/detail")
    public ResponseEntity<ActivityDetailResponse> getActivityDetail(
            @RequestBody AdminActivityDetailRequest request
            ) {
        ActivityDetailResponse response = adminService.getActivityDetail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/block")
    public ResponseEntity<AdminActivityCreationResponse> blockUser(
            @RequestBody AdminBlockUnblockUserRequest request
            ) {
        AdminActivityCreationResponse response = adminService.adminBlockUser(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/user/unblock")
    public ResponseEntity<AdminActivityCreationResponse> unblockUser(
            @RequestBody AdminBlockUnblockUserRequest request
    ) {
        AdminActivityCreationResponse response = adminService.adminUnblockUser(request);
        return ResponseEntity.ok(response);
    }
}
