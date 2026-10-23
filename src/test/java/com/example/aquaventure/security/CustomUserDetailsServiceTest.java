package com.example.aquaventure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.UserRepository;

class CustomUserDetailsServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

    @Test
    void loadUserByUsername_found_returnsPrincipal() {
        User user = new User();
        user.setId(1L);
        user.setEmail("surfer@example.com");
        user.setPassword("hashed");
        user.setRole(Role.SURFER);
        when(userRepository.findByEmail("surfer@example.com")).thenReturn(Optional.of(user));

        UserPrincipal principal = (UserPrincipal) service.loadUserByUsername("surfer@example.com");

        assertThat(principal.getId()).isEqualTo(1L);
        assertThat(principal.getRole()).isEqualTo(Role.SURFER);
    }

    @Test
    void loadUserByUsername_notFound_throws() {
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("ghost@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
