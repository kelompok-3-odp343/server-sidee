package com.example.wandoor.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.model.response.UserListAdminResponse;
import com.example.wandoor.service.UserListAdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UserListAdminController {

    private final UserListAdminService service;

    @GetMapping("/users-list")
    public ResponseEntity<UserListAdminResponse> getUsersList(Authentication authentication) {
        String userId = authentication.getName();
        return ResponseEntity.ok(service.getAllUsersList(userId));
    }
}
