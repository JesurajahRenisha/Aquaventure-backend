package com.example.aquaventure.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.aquaventure.dto.admin.SchoolSummaryDto;
import com.example.aquaventure.dto.admin.UserSummaryDto;
import com.example.aquaventure.dto.auth.LoginRequest;
import com.example.aquaventure.dto.auth.LoginResponse;
import com.example.aquaventure.dto.auth.RegisterSchoolRequest;
import com.example.aquaventure.dto.auth.RegisterSurferRequest;
import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.mapper.SurfSchoolMapper;
import com.example.aquaventure.mapper.UserMapper;
import com.example.aquaventure.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/surfer")
    public ResponseEntity<ApiResponse<UserSummaryDto>> registerSurfer(@Valid @RequestBody RegisterSurferRequest request) {
        User user = authService.registerSurfer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(UserMapper.toSummaryDto(user)));
    }

    @PostMapping(value = "/register/school", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<SchoolSummaryDto>> registerSchool(
            @Valid @ModelAttribute RegisterSchoolRequest request,
            @RequestPart(value = "documents", required = false) List<MultipartFile> documents) {
        SurfSchool school = authService.registerSchool(request, documents);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(SurfSchoolMapper.toSummaryDto(school)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authService.login(request)));
    }
}
