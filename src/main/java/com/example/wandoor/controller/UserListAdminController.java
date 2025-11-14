package com.example.wandoor.controller;

import com.example.wandoor.service.UserListAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // ✅ Tambahkan ini
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserListAdminController {

    private final UserListAdminService service;

    @GetMapping("/users-list")
    public ResponseEntity<?> getUsersList(Authentication authentication) {
        String userId = authentication.getName(); // ✅ userId dari JWT
        return ResponseEntity.ok(service.getAllUsersList(userId));
    }
}
