package com.example.aquaventure.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.aquaventure.repository.UserRepository;

class PlatformAdminSeederTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private PlatformAdminSeeder seederWith(String email, String password) throws Exception {
        PlatformAdminSeeder seeder = new PlatformAdminSeeder(userRepository, passwordEncoder);
        setField(seeder, "email", email);
        setField(seeder, "password", password);
        setField(seeder, "firstname", "Platform");
        setField(seeder, "lastname", "Admin");
        return seeder;
    }

    private void setField(Object target, String name, Object value) throws Exception {
        Field field = PlatformAdminSeeder.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void run_whenAdminAbsent_createsAdmin() throws Exception {
        PlatformAdminSeeder seeder = seederWith("admin@aquaventure.dev", "Admin@12345");
        when(userRepository.existsByEmail("admin@aquaventure.dev")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        seeder.run(null);

        verify(userRepository).save(any());
    }

    @Test
    void run_whenAdminAlreadyExists_skipsCreation() throws Exception {
        PlatformAdminSeeder seeder = seederWith("admin@aquaventure.dev", "Admin@12345");
        when(userRepository.existsByEmail("admin@aquaventure.dev")).thenReturn(true);

        seeder.run(null);

        verify(userRepository, never()).save(any());
    }

    @Test
    void run_whenEmailBlank_skipsCreation() throws Exception {
        PlatformAdminSeeder seeder = seederWith("", "");

        seeder.run(null);

        verify(userRepository, never()).save(any());
    }
}
