// src/main/java/com/example/wandoor/controller/TrxSummaryController.java
package com.example.wandoor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.model.response.TrxSummaryResponse;
import com.example.wandoor.service.TrxSummaryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class TrxSummaryController {
//hshs
    private final TrxSummaryService service;

    @PreAuthorize("hasAnyRole('MAKER','CHECKER','APPROVAL')")
    @GetMapping("/transaction/summary")
    public ResponseEntity<TrxSummaryResponse> getOverview() {
        return ResponseEntity.ok(service.getOverview());
    }
}