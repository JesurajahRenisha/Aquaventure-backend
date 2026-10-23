package com.example.aquaventure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.User;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @org.springframework.beans.factory.annotation.Autowired
    private UserRepository userRepository;

    private User user(String email) {
        User user = new User();
        user.setFirstname("Jane");
        user.setLastname("Doe");
        user.setEmail(email);
        user.setPassword("Hashed1!");
        user.setRole(Role.SURFER);
        return user;
    }

    @Test
    void duplicateEmail_violatesUniqueConstraint() {
        userRepository.saveAndFlush(user("dup@example.com"));

        assertThatThrownBy(() -> userRepository.saveAndFlush(user("dup@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findByEmail_returnsSavedUser() {
        userRepository.saveAndFlush(user("find-me@example.com"));

        assertThat(userRepository.findByEmail("find-me@example.com")).isPresent();
        assertThat(userRepository.existsByEmail("find-me@example.com")).isTrue();
    }
}
