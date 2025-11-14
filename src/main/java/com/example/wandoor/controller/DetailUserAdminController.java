package com.example.wandoor.controller;

import com.example.wandoor.model.request.DetailUserAdminRequest;
import com.example.wandoor.model.response.DetailUserAdminResponse;
import com.example.wandoor.service.AdminMenuAccessService;
import com.example.wandoor.service.DetailUserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DetailUserAdminController {

    private final DetailUserAdminService detailUserAdminService;
    private final AdminMenuAccessService adminMenuAccessService;

    @PostMapping("/detail-user")
    public ResponseEntity<DetailUserAdminResponse> getDetail(@RequestBody DetailUserAdminRequest request) {
        DetailUserAdminResponse response = detailUserAdminService.getUserDetail(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/menu/access")
    public ResponseEntity<Map<String, Map<String, String>>> getMenu(){
        Map<String, Map<String, String>> response = adminMenuAccessService.getMenuStructure();
        return ResponseEntity.ok(response);
    }



}
