package com.example.wandoor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.model.response.SavingDetailResponse;
import com.example.wandoor.service.SavingDetailService;
import com.example.wandoor.service.SavingsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SavingNDetailController {
    private final SavingsService savingsService;
    private final SavingDetailService savingDetailService;


    // SAVINGS 
    @PostMapping("/savings")
    public ResponseEntity<?> getSavingsForLoggedInUser() {
        var response = savingsService.getSavingsForLoggedInUser();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/savings/detail")
    public ResponseEntity<SavingDetailResponse> getSavingsDetail() {
        var response = savingDetailService.getSavingsDetail(null, null);
        return ResponseEntity.ok(response);
    }

}