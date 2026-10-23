package com.example.aquaventure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.example.aquaventure.dto.auth.LoginRequest;
import com.example.aquaventure.dto.auth.LoginResponse;
import com.example.aquaventure.dto.auth.RegisterSchoolRequest;
import com.example.aquaventure.dto.auth.RegisterSurferRequest;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.exception.DuplicateResourceException;
import com.example.aquaventure.repository.UserRepository;
import com.example.aquaventure.security.JwtUtil;
import com.example.aquaventure.service.storage.FileStorageService;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder encoder = mock(PasswordEncoder.class);
    private final JwtUtil jwtUtil = mock(JwtUtil.class);
    private final SurfSchoolService surfSchoolService = mock(SurfSchoolService.class);
    private final SurfLocationService surfLocationService = mock(SurfLocationService.class);
    private final FileStorageService fileStorageService = mock(FileStorageService.class);

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, encoder, jwtUtil, surfSchoolService,
                surfLocationService, fileStorageService);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getId() == null) {
                u.setId(1L);
            }
            return u;
        });
        when(encoder.encode(any())).thenReturn("hashed");
    }

    private RegisterSurferRequest surferRequest() {
        RegisterSurferRequest request = new RegisterSurferRequest();
        request.setFirstname("Jane");
        request.setLastname("Doe");
        request.setEmail("Jane.Doe@Example.com");
        request.setPassword("Password1!");
        return request;
    }

    @Test
    void registerSurfer_success_savesActiveSurferWithNormalizedEmail() {
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(false);

        User saved = authService.registerSurfer(surferRequest());

        assertThat(saved.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(saved.getRole()).isEqualTo(Role.SURFER);
        assertThat(saved.getPassword()).isEqualTo("hashed");
    }

    @Test
    void registerSurfer_duplicateEmail_throws() {
        when(userRepository.existsByEmail("jane.doe@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.registerSurfer(surferRequest()))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void registerSchool_createsOwnerAndPendingSchool() {
        RegisterSchoolRequest request = new RegisterSchoolRequest();
        request.setOwnerFirstname("Kasun");
        request.setOwnerLastname("Perera");
        request.setEmail("kasun@example.com");
        request.setPassword("Password1!");
        request.setSchoolName("Arugam Surf Co");
        request.setBusinessRegistrationNumber("BRN123");

        when(userRepository.existsByEmail("kasun@example.com")).thenReturn(false);
        SurfSchool pendingSchool = new SurfSchool();
        pendingSchool.setStatus(SchoolStatus.PENDING);
        when(surfSchoolService.createPending(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(pendingSchool);

        SurfSchool result = authService.registerSchool(request, null);

        assertThat(result.getStatus()).isEqualTo(SchoolStatus.PENDING);
    }

    @Test
    void login_success_includesRole() {
        User user = new User();
        user.setId(5L);
        user.setEmail("surfer@example.com");
        user.setPassword("hashed");
        user.setRole(Role.SURFER);
        when(userRepository.findByEmail("surfer@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("Password1!", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("a-token");

        LoginRequest request = new LoginRequest();
        request.setEmail("surfer@example.com");
        request.setPassword("Password1!");

        LoginResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("a-token");
        assertThat(response.getRole()).isEqualTo(Role.SURFER);
        assertThat(response.getSchoolId()).isNull();
    }

    @Test
    void login_wrongPassword_throws401() {
        User user = new User();
        user.setId(5L);
        user.setEmail("surfer@example.com");
        user.setPassword("hashed");
        user.setRole(Role.SURFER);
        when(userRepository.findByEmail("surfer@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong", "hashed")).thenReturn(false);

        LoginRequest request = new LoginRequest();
        request.setEmail("surfer@example.com");
        request.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(request)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void login_schoolAdmin_includesSchoolStatus() {
        User user = new User();
        user.setId(9L);
        user.setEmail("owner@example.com");
        user.setPassword("hashed");
        user.setRole(Role.SCHOOL_ADMIN);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(user));
        when(encoder.matches("Password1!", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken(user)).thenReturn("a-token");

        SurfSchool school = new SurfSchool();
        school.setId(3L);
        school.setStatus(SchoolStatus.PENDING);
        when(surfSchoolService.findByOwnerUserId(9L)).thenReturn(school);

        LoginRequest request = new LoginRequest();
        request.setEmail("owner@example.com");
        request.setPassword("Password1!");

        LoginResponse response = authService.login(request);

        assertThat(response.getSchoolId()).isEqualTo(3L);
        assertThat(response.getSchoolStatus()).isEqualTo(SchoolStatus.PENDING);
    }
}
