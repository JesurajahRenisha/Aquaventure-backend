package com.aquaventure.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.LoginResponse;
import com.aquaventure.dto.RegisterRequest;
import com.aquaventure.dto.ResetPasswordRequest;
import com.aquaventure.dto.UserResponse;
import com.aquaventure.entity.Instructor;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.Role;
import com.aquaventure.entity.Tourist;
import com.aquaventure.entity.User;
import com.aquaventure.exception.ConflictException;
import com.aquaventure.exception.ForbiddenActionException;
import com.aquaventure.exception.ResourceNotFoundException;
import com.aquaventure.mapper.AuthMapper;
import com.aquaventure.repository.InstructorRepository;
import com.aquaventure.repository.ProviderRepository;
import com.aquaventure.repository.TouristRepository;
import com.aquaventure.repository.UserRepository;
import com.aquaventure.security.JwtUtil;

@Service
@Transactional
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TouristRepository touristRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private InstructorRepository instructorRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JwtUtil jwtUtil;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already registered");
        }
        if (request.getRole() == Role.ADMIN) {
            throw new ForbiddenActionException("Admin accounts cannot self-register");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(request.getRole());
        userRepository.save(user);

        createProfileForRole(user, request);

        return AuthMapper.toUserResponse(user);
    }

    private void createProfileForRole(User user, RegisterRequest request) {
        switch (user.getRole()) {
            case SURFER -> {
                Tourist tourist = new Tourist();
                tourist.setUser(user);
                tourist.setSkillLevel(request.getSkillLevel());
                tourist.setExperience(request.getExperience());
                touristRepository.save(tourist);
            }
            case PROVIDER -> {
                Provider provider = new Provider();
                provider.setUser(user);
                provider.setBusinessName(request.getBusinessName());
                provider.setContactDetails(request.getContactDetails());
                provider.setLocation(request.getLocation());
                providerRepository.save(provider);
            }
            case INSTRUCTOR -> {
                if (request.getProviderId() == null) {
                    throw new ConflictException("providerId is required when registering as an instructor");
                }
                Provider provider = providerRepository.findById(request.getProviderId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Provider not found: " + request.getProviderId()));
                Instructor instructor = new Instructor();
                instructor.setUser(user);
                instructor.setProvider(provider);
                instructor.setName(user.getName());
                instructorRepository.save(instructor);
            }
            case ADMIN -> {
                // unreachable: self-registration as ADMIN is rejected above.
            }
        }
    }

    public LoginResponse login(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!encoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new ForbiddenActionException("This account has been suspended");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole());
        Long profileId = resolveProfileId(user);

        return new LoginResponse(token, user.getUserId(), user.getName(), user.getEmail(), user.getRole(), profileId);
    }

    private Long resolveProfileId(User user) {
        return switch (user.getRole()) {
            case SURFER -> touristRepository.findByUser(user).map(Tourist::getTouristId).orElse(null);
            case PROVIDER -> providerRepository.findByUser(user).map(Provider::getProviderId).orElse(null);
            case INSTRUCTOR -> instructorRepository.findByUser(user).map(Instructor::getInstructorId).orElse(null);
            case ADMIN -> null;
        };
    }

    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("No account found for that email"));
        user.setPassword(encoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
