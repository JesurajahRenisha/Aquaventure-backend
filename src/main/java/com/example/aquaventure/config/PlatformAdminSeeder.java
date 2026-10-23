package com.example.aquaventure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.entity.UserStatus;
import com.example.aquaventure.repository.UserRepository;

/** Idempotent: safe to run on every startup, only creates the account when it is missing. */
@Component
public class PlatformAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(PlatformAdminSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${platform-admin.email:}")
    private String email;

    @Value("${platform-admin.password:}")
    private String password;

    @Value("${platform-admin.firstname:Platform}")
    private String firstname;

    @Value("${platform-admin.lastname:Admin}")
    private String lastname;

    public PlatformAdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            log.warn("platform-admin.email/password not set - skipping platform admin seeding");
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        User admin = new User();
        admin.setFirstname(firstname);
        admin.setLastname(lastname);
        admin.setEmail(normalizedEmail);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.PLATFORM_ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setLocal(true);
        userRepository.save(admin);
        log.info("Seeded platform admin account: {}", normalizedEmail);
    }
}
