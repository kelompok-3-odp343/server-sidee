package com.example.wandoor.controller;

import com.example.wandoor.model.response.ProfileResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.wandoor.service.ProfileService;

@RestController
@RequestMapping("/api/v1")
public class ProfileController{
    private final ProfileService service;

    public ProfileController(ProfileService service){
        this.service = service;
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(){
       var response = service.getProfile();
               return ResponseEntity.ok(response);
    }
}