package com.example.aquaventure.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.aquaventure.dto.admin.UserSummaryDto;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.entity.UserStatus;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.UserMapper;
import com.example.aquaventure.repository.UserRepository;

@Service
public class UserAdminService {

    private final UserRepository userRepository;

    public UserAdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<UserSummaryDto> search(Role role, UserStatus status, String q, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (q != null && !q.isBlank()) {
            String pattern = "%" + q.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("firstname")), pattern),
                    cb.like(cb.lower(root.get("lastname")), pattern)));
        }

        return userRepository.findAll(spec, pageable).map(UserMapper::toSummaryDto);
    }

    public User getByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    public UserSummaryDto suspend(Long id) {
        User user = getByIdOrThrow(id);
        user.setStatus(UserStatus.SUSPENDED);
        return UserMapper.toSummaryDto(userRepository.save(user));
    }

    public UserSummaryDto reactivate(Long id) {
        User user = getByIdOrThrow(id);
        user.setStatus(UserStatus.ACTIVE);
        return UserMapper.toSummaryDto(userRepository.save(user));
    }
}
