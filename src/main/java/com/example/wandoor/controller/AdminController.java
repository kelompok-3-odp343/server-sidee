package com.example.wandoor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.model.request.AdminTransactionListRequest;
import com.example.wandoor.model.request.DetailUserAdminRequest;
import com.example.wandoor.model.response.AdminTransactionListResponse;
import com.example.wandoor.model.response.DetailUserAdminResponse;
import com.example.wandoor.service.AdminTransactionListService;
import com.example.wandoor.service.DetailUserAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DetailUserAdminService detailUserAdminService;
    private final AdminTransactionListService service;

    @PostMapping("/detail-user")
    public ResponseEntity<DetailUserAdminResponse> getDetail(@RequestBody DetailUserAdminRequest request) {
        DetailUserAdminResponse response = detailUserAdminService.getUserDetail(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transaction/list")
    public ResponseEntity<AdminTransactionListResponse> list(@RequestBody AdminTransactionListRequest request){
        return ResponseEntity.ok(service.getTransactionList(request));
    }
}

