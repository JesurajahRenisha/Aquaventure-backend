package com.aquaventure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.UserResponse;
import com.aquaventure.dto.UserUpdateRequest;
import com.aquaventure.entity.User;
import com.aquaventure.mapper.AuthMapper;
import com.aquaventure.repository.UserRepository;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserResponse getMe(User user) {
        return AuthMapper.toUserResponse(user);
    }

    /** Admin-only: list every account on the platform. */
    public List<UserResponse> listAll() {
        return userRepository.findAll().stream().map(AuthMapper::toUserResponse).toList();
    }

    public UserResponse updateMe(User user, UserUpdateRequest request) {
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        return AuthMapper.toUserResponse(userRepository.save(user));
    }
}
