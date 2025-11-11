package com.example.wandoor.controller;

import com.example.wandoor.model.request.DetailUserAdminRequest;
import com.example.wandoor.model.response.DetailUserAdminResponse;
import com.example.wandoor.service.DetailUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DetailUserAdminService detailUserAdminService;

    @PostMapping("/detail-user")
    public ResponseEntity<DetailUserAdminResponse> getDetail(@RequestBody DetailUserAdminRequest request) {
        DetailUserAdminResponse response = detailUserAdminService.getUserDetail(request);
        return ResponseEntity.ok(response);
    }
}
