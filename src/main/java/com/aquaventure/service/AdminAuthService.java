package com.aquaventure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.AdminLoginResponse;
import com.aquaventure.dto.AdminResponse;
import com.aquaventure.entity.Admin;
import com.aquaventure.exception.ConflictException;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.repository.AdminRepository;
import com.aquaventure.security.JwtUtil;

@Service
@Transactional
public class AdminAuthService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${admin.setup-key}")
    private String setupKey;

    public AdminResponse register(String email, String password, String providedSetupKey) {
        if (providedSetupKey == null || !providedSetupKey.equals(setupKey)) {
            throw new ForbiddenActionException("Invalid setup key");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (adminRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ConflictException("Email already registered");
        }

        Admin admin = new Admin();
        admin.setEmail(normalizedEmail);
        admin.setPassword(encoder.encode(password));
        adminRepository.save(admin);

        return new AdminResponse(admin.getId(), admin.getEmail());
    }

    public AdminLoginResponse login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        Admin admin = adminRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!encoder.matches(password, admin.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.generateAdminToken(admin.getId(), admin.getEmail());
        return new AdminLoginResponse(token, admin.getId(), admin.getEmail());
    }
}
