package com.example.wandoor.controller;

import com.example.wandoor.model.request.TransactionHistoryRequest;
import com.example.wandoor.model.response.DetailTrxResponse;
import com.example.wandoor.model.response.TransactionHistoryResponse;
import com.example.wandoor.service.TransactionHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class TrxHistoryController {

    private final TransactionHistoryService service;

    @PostMapping("/trx-history")
    ResponseEntity<TransactionHistoryResponse> fetchTransactionHistory(
            @Valid @RequestBody TransactionHistoryRequest request){
//        log.info("Incoming request = {}", request);
        var response = service.fetchTransactionHistory(request);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/trx-history/{transactionId}")
    ResponseEntity<DetailTrxResponse> detailTransactionHistory(
            @PathVariable String transactionId){
        var response = service.fetchTransactionDetail(transactionId);
        return ResponseEntity.ok(response);
    }
}