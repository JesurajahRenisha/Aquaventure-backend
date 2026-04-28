package com.example.aquaventure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.UserRepository;
import com.example.aquaventure.security.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String register(User user){
        // Additional business logic validations
        validateRegistrationData(user);

        // Check if email already exists
        if (repo.findByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        // Sanitize input data
        sanitizeUserData(user);

        // Encode password
        user.setPassword(encoder.encode(user.getPassword()));

        // Save user
        repo.save(user);
        return "User registered successfully";
    }

    public String login(String email, String password){
        // Basic validation for login (email and password are required)
        if (email == null || email.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        // Sanitize email
        email = email.trim().toLowerCase();

        // Find user by email
        User user = repo.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        // Verify password
        if(!encoder.matches(password, user.getPassword())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        // Generate and return JWT token
        return jwtUtil.generatedToken(email);
    }

    private void validateRegistrationData(User user) {
        // Additional validations beyond Bean Validation annotations
        if (user.getFirstname() != null && user.getFirstname().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "First name is too long");
        }
        if (user.getLastname() != null && user.getLastname().length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Last name is too long");
        }
        if (user.getEmail() != null && user.getEmail().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is too long");
        }
        if (user.getPassword() != null && user.getPassword().length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is too long");
        }
    }

    private void sanitizeUserData(User user) {
        // Trim whitespace and normalize data
        if (user.getFirstname() != null) {
            user.setFirstname(user.getFirstname().trim());
        }
        if (user.getLastname() != null) {
            user.setLastname(user.getLastname().trim());
        }
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }
        // Password sanitization is handled by encoding, no need to trim
    }
}
