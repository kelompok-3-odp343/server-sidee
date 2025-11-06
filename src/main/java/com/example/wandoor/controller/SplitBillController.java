package com.example.wandoor.controller;

import com.example.wandoor.model.request.EditSplitBillRequest;
import com.example.wandoor.model.request.PatchSplitBillRequest;
import com.example.wandoor.model.request.SplitBillDetailRequest;
import com.example.wandoor.model.response.*;
import com.example.wandoor.service.SplitBillService;

import com.example.wandoor.model.request.AddNewSplitBillRequest;
import com.example.wandoor.model.response.SplitBillsListResponse;
import com.example.wandoor.service.SplitBillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("api/split-bill")
@RequiredArgsConstructor
@Log4j2
public class SplitBillController {
    private final SplitBillService splitBillService;

    @GetMapping
    public ResponseEntity<SplitBillsListResponse> getAllSplitBills(){
        SplitBillsListResponse response = splitBillService.getAllSplitBill();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/detail")
    public ResponseEntity<SplitBillDetailResponse> getAllSplitBillMember(@Valid @RequestBody SplitBillDetailRequest request) {
        SplitBillDetailResponse response = splitBillService.getAllSplitBillMember(request);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/add")
    public ResponseEntity<AddNewSplitBillResponse> createSplitBill(
            @RequestBody AddNewSplitBillRequest request){

        System.out.println("Split Bill Controller");
        log.info("Receive create split bill request: {}", request.splitBillTitle());
        AddNewSplitBillResponse response = splitBillService.createSplitBill(request);

        return ResponseEntity
                .created(URI.create("api/split-bill" + response.splitBillId()))
                .body(response);
    }

    @PutMapping("/edit")
    public ResponseEntity<EditSplitBillResponse> editSplitBill(
            @Valid @RequestBody EditSplitBillRequest request){

//        log.info("Receive create split bill request: {}", request.splitBillTitle());

        EditSplitBillResponse response = splitBillService.editSplitBill(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/mark-paid")
    public ResponseEntity<Void> markAsPaid(
        @Valid @RequestBody PatchSplitBillRequest request){
            splitBillService.updateHaspaidSplitBill(request);
            return ResponseEntity.noContent().build();
        }
}
