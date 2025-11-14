package com.example.wandoor.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.model.request.AdminApproverListRequest;
import com.example.wandoor.model.request.AdminTransactionListRequest;
import com.example.wandoor.model.request.DetailUserAdminRequest;
import com.example.wandoor.model.response.AdminApproverDataResponse;
import com.example.wandoor.model.response.AdminTransactionListResponse;
import com.example.wandoor.model.response.DetailUserAdminResponse;
import com.example.wandoor.model.response.GenericResponse;
import com.example.wandoor.service.AdminApproverListService;
import com.example.wandoor.service.AdminMenuAccessService;
import com.example.wandoor.service.AdminTransactionListService;
import com.example.wandoor.service.DetailUserAdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.model.request.AdminApproverListRequest;
import com.example.wandoor.model.request.DetailUserAdminRequest;
import com.example.wandoor.model.request.TransactionListRequest;
import com.example.wandoor.model.response.AdminApproverDataResponse;
import com.example.wandoor.model.response.DetailUserAdminResponse;
import com.example.wandoor.model.response.GenericResponse;
import com.example.wandoor.model.response.TransactionListResponse;
import com.example.wandoor.service.AdminApproverListService;
import com.example.wandoor.service.AdminMenuAccessService;
import com.example.wandoor.service.DetailUserAdminService;
import com.example.wandoor.service.TransactionListService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DetailUserAdminService detailUserAdminService;
    private final AdminTransactionListService service;
    private final AdminMenuAccessService adminMenuAccessService;
    private final AdminApproverListService adminApproverListService;
    private final TransactionListService transactionService;

    @PostMapping("/detail-user")
    public ResponseEntity<DetailUserAdminResponse> getDetail(@RequestBody DetailUserAdminRequest request) {
        DetailUserAdminResponse response = detailUserAdminService.getUserDetail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transaction/list")
    public ResponseEntity<AdminTransactionListResponse> list(@RequestBody AdminTransactionListRequest request){
        return ResponseEntity.ok(service.getTransactionList(request));
    }

    @GetMapping("/menu/access")
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



    @PostMapping("/transaction/list")
    @PreAuthorize("hasAnyRole('MAKER', 'CHECKER', 'APPROVAL')")
    public ResponseEntity<TransactionListResponse> getTransactionList(@RequestBody TransactionListRequest request) {
        log.info("📩 Request received to fetch transaction list for userId={}", request.getUserId());
        TransactionListResponse response = transactionService.getTransactionList(request);
        return ResponseEntity.ok(response);
    }
}
