package com.example.aquaventure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.example.aquaventure.repository.BookingRepository;
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.UserRepository;

@SpringBootTest
@ActiveProfiles({"test", "dev"})
class DevDataSeederTest {

    @Autowired
    private DevDataSeeder devDataSeeder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SurfSchoolRepository surfSchoolRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void contextStartup_seedsSchoolsInstructorsLessonsAndBookings() {
        assertThat(userRepository.existsByEmail("seed.arugam.owner@aquaventure.dev")).isTrue();
        assertThat(userRepository.existsByEmail("seed.surfer@aquaventure.dev")).isTrue();
        assertThat(surfSchoolRepository.count()).isGreaterThanOrEqualTo(3);
        assertThat(bookingRepository.count()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void runningTwice_isIdempotent() {
        long schoolCountBefore = surfSchoolRepository.count();

        devDataSeeder.run(null);

        assertThat(surfSchoolRepository.count()).isEqualTo(schoolCountBefore);
    }
}
