package com.example.wandoor.controller;

import com.example.wandoor.service.UserListAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserListAdminController {

    private final UserListAdminService service;

    @GetMapping("/users-list")
    public ResponseEntity<Map<String, Object>> getUsersList(
            @RequestHeader("User-Id") String userId) {
        return ResponseEntity.ok(service.getAllUsersList(userId));
    }
}
