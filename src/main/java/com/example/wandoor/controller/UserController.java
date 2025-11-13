package com.example.wandoor.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.model.request.AccountRequest;
import com.example.wandoor.model.request.LifegoalsDetailsRequest;
import com.example.wandoor.model.request.SavingDetailRequest;
import com.example.wandoor.model.request.TransactionHistoryRequest;
import com.example.wandoor.model.response.AccountResponse;
import com.example.wandoor.model.response.DepositResponse;
import com.example.wandoor.model.response.DplkListResponse;
import com.example.wandoor.model.response.FetchDashboardResponse;
import com.example.wandoor.model.response.LifegoalsDetailsResponse;
import com.example.wandoor.model.response.LifegoalsGroupResponse;
import com.example.wandoor.model.response.ProfileResponse;
import com.example.wandoor.model.response.SavingDetailResponse;
import com.example.wandoor.model.response.TransactionHistoryResponse;
import com.example.wandoor.service.AccountService;
import com.example.wandoor.service.DashboardService;
import com.example.wandoor.service.DepositService;
import com.example.wandoor.service.DplkService;
import com.example.wandoor.service.LifegoalsService;
import com.example.wandoor.service.ProfileService;
import com.example.wandoor.service.SavingDetailService;
import com.example.wandoor.service.SavingsService;
import com.example.wandoor.service.TransactionHistoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;
    private final AccountService accountService;
    private final SavingsService savingsService;
    private final SavingDetailService savingDetailService;
    private final DepositService depositService;
    private final DplkService dplkService;
    private final DashboardService dashboardService;
    private final TransactionHistoryService transactionHistoryService;
    private final LifegoalsService lifegoalsService;

    //DASHBOARD 
    @GetMapping("/fetch-dashboard")
    public ResponseEntity<FetchDashboardResponse> fetchDashboard() {
        var response = dashboardService.fetchDashboard();
        return ResponseEntity.ok(response);
    }

    // PROFILE
    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        var response = profileService.getProfile();
        return ResponseEntity.ok(response);
    }

    // ACCOUNT

    @PostMapping("/account")
    public ResponseEntity<AccountResponse> getAccountData(
            @RequestBody(required = false) AccountRequest request) {
        var response = accountService.dataAccount(request);
        return ResponseEntity.ok(response);
    }

    // SAVINGS 
    @PostMapping("/savings")
    public ResponseEntity<?> getSavingsForLoggedInUser() {
        var response = savingsService.getSavingsForLoggedInUser();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/savings/detail")
    public ResponseEntity<SavingDetailResponse> getSavingsDetail(
            @RequestBody(required = false) SavingDetailRequest request) {
        var response = savingDetailService.getSavingsDetail(request);
        return ResponseEntity.ok(response);
    }

    // DEPOSIT 
    @GetMapping("/detail-deposit")
    public ResponseEntity<DepositResponse> fetchDeposit() {
        var response = depositService.fetchDeposit();
        return ResponseEntity.ok(response);
    }

    //DPLK
    @GetMapping("/dplk")
    public ResponseEntity<DplkListResponse> fetchDplkData() {
        var response = dplkService.fetchDplkData();
        return ResponseEntity.ok(response);
    }

    //TRANSACTION HISTORY
    @PostMapping("/trx-history")
    public ResponseEntity<TransactionHistoryResponse> fetchTransactionHistory(
            @Valid @RequestBody TransactionHistoryRequest request) {
        var response = transactionHistoryService.fetchTransactionHistory(request);
        return ResponseEntity.ok(response);
    }

    //LIFEGOALS

    @GetMapping("/lifegoals")
    public ResponseEntity<Map<String, LifegoalsGroupResponse>> fetchAllLifegoals() {
        var response = lifegoalsService.fetchAllLifegoals();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/lifegoals-detail")
    public ResponseEntity<LifegoalsDetailsResponse> fetchDetailLifegoals(
            @RequestBody LifegoalsDetailsRequest request) {
        var response = lifegoalsService.fetchDetailLifegoals(request);
        return ResponseEntity.ok(response);
    }
}
