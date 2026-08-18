package com.aquaventure.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.AdminLoginRequest;
import com.aquaventure.dto.AdminLoginResponse;
import com.aquaventure.dto.AdminRegisterRequest;
import com.aquaventure.dto.AdminResponse;
import com.aquaventure.service.AdminAuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Authentication")
public class AdminAuthController {

    @Autowired
    private AdminAuthService adminAuthService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AdminResponse register(@Valid @RequestBody AdminRegisterRequest request) {
        return adminAuthService.register(request.getEmail(), request.getPassword(), request.getSetupKey());
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
        return adminAuthService.login(request.getEmail(), request.getPassword());
    }
}
