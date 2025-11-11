package com.example.wandoor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.model.request.SavingsRequest;
import com.example.wandoor.model.response.SavingsResponse;
import com.example.wandoor.service.SavingsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class SavingsController {

    private final SavingsService savingsService;

    @PostMapping("/savings/detail")
public ResponseEntity<SavingsResponse> getSavingsDetail(@RequestBody(required = false) SavingsRequest request) {
    var response = savingsService.getSavingsDetail(request);
    return ResponseEntity.ok(response);
}
}
