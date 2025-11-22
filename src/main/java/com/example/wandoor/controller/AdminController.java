package com.example.wandoor.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.model.request.AdminActivityDetailRequest;
import com.example.wandoor.model.request.AdminApprovalRequest;
import com.example.wandoor.model.request.AdminApproverListRequest;
import com.example.wandoor.model.request.AdminBlockUnblockUserRequest;
import com.example.wandoor.model.request.AdminTransactionListRequest;
import com.example.wandoor.model.request.DetailUserAdminRequest;
import com.example.wandoor.model.response.ActivityDetailResponse;
import com.example.wandoor.model.response.ActivityListResponse;
import com.example.wandoor.model.response.AdminActivityCreationResponse;
import com.example.wandoor.model.response.AdminApprovalResponse;
import com.example.wandoor.model.response.AdminApproverDataResponse;
import com.example.wandoor.model.response.AdminTransactionListResponse;
import com.example.wandoor.model.response.DetailUserAdminResponse;
import com.example.wandoor.model.response.GenericResponse;
import com.example.wandoor.service.AdminApproverListService;
import com.example.wandoor.service.AdminMenuAccessService;
import com.example.wandoor.service.AdminService;
import com.example.wandoor.service.AdminTransactionListService;
import com.example.wandoor.service.DetailUserAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DetailUserAdminService detailUserAdminService;
    private final AdminMenuAccessService adminMenuAccessService;
    private final AdminApproverListService adminApproverListService;
    private final AdminService adminService;
    private final AdminTransactionListService adminTransactionListService;

    @PostMapping("/detail-user")
    public ResponseEntity<DetailUserAdminResponse> getDetail(@RequestBody DetailUserAdminRequest request) {
        DetailUserAdminResponse response = detailUserAdminService.getUserDetail(request);
        return ResponseEntity.ok(response);
   }

    @GetMapping("/menu-access")
    public ResponseEntity<Map<String, Map<String, Map<String, Object>>>> getMenu(){
        Map<String, Map<String, Map<String, Object>>> response = adminMenuAccessService.getMenuStructure();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transaction/list")
    public ResponseEntity<AdminTransactionListResponse> list(@RequestBody AdminTransactionListRequest request){
        return ResponseEntity.ok(adminTransactionListService.getTransactionList(request));
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

    @PostMapping("/user/approval")
    public ResponseEntity<AdminApprovalResponse> approveActivity(
            @RequestBody AdminApprovalRequest request
    ) {
        AdminApprovalResponse response = adminService.approveActivity(request);
        return ResponseEntity.ok(response);
    }
}
