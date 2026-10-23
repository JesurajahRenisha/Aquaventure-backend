package com.example.aquaventure.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.aquaventure.dto.auth.LoginRequest;
import com.example.aquaventure.dto.auth.LoginResponse;
import com.example.aquaventure.dto.auth.RegisterSchoolRequest;
import com.example.aquaventure.dto.auth.RegisterSurferRequest;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.exception.DuplicateResourceException;
import com.example.aquaventure.repository.UserRepository;
import com.example.aquaventure.security.JwtUtil;
import com.example.aquaventure.service.storage.FileStorageService;
import com.example.aquaventure.service.storage.StoredFile;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final SurfSchoolService surfSchoolService;
    private final SurfLocationService surfLocationService;
    private final FileStorageService fileStorageService;

    public AuthService(UserRepository userRepository, PasswordEncoder encoder, JwtUtil jwtUtil,
                        SurfSchoolService surfSchoolService, SurfLocationService surfLocationService,
                        FileStorageService fileStorageService) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
        this.surfSchoolService = surfSchoolService;
        this.surfLocationService = surfLocationService;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public User registerSurfer(RegisterSurferRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setFirstname(request.getFirstname().trim());
        user.setLastname(request.getLastname().trim());
        user.setEmail(email);
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(Role.SURFER);
        user.setPhone(request.getPhone());
        user.setNationality(request.getNationality());
        user.setLocal(request.isLocal());

        return userRepository.save(user);
    }

    @Transactional
    public SurfSchool registerSchool(RegisterSchoolRequest request, List<MultipartFile> documents) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }

        User owner = new User();
        owner.setFirstname(request.getOwnerFirstname().trim());
        owner.setLastname(request.getOwnerLastname().trim());
        owner.setEmail(email);
        owner.setPassword(encoder.encode(request.getPassword()));
        owner.setRole(Role.SCHOOL_ADMIN);
        owner.setPhone(request.getPhone());
        owner.setNationality(request.getNationality());
        owner.setLocal(request.isLocal());
        owner = userRepository.save(owner);

        List<SurfLocation> locations = new ArrayList<>();
        if (request.getLocationIds() != null) {
            for (Long locationId : request.getLocationIds()) {
                locations.add(surfLocationService.getByIdOrThrow(locationId));
            }
        }

        List<String> documentUrls = new ArrayList<>();
        if (documents != null) {
            for (MultipartFile file : documents) {
                if (file != null && !file.isEmpty()) {
                    StoredFile stored = fileStorageService.store(file, "school-documents", email);
                    documentUrls.add(stored.storedPath());
                }
            }
        }

        return surfSchoolService.createPending(owner, request.getSchoolName(), request.getDescription(),
                request.getAddress(), request.getContactPhone(), request.getContactEmail(),
                request.getBusinessRegistrationNumber(), locations, documentUrls);
    }

    public LoginResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        String token = jwtUtil.generateToken(user);
        LoginResponse response = new LoginResponse(token, user.getId(), user.getEmail(),
                user.getFirstname(), user.getLastname(), user.getRole());

        if (user.getRole() == Role.SCHOOL_ADMIN) {
            SurfSchool school = surfSchoolService.findByOwnerUserId(user.getId());
            if (school != null) {
                response.setSchoolId(school.getId());
                response.setSchoolStatus(school.getStatus());
            }
        }

        return response;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
