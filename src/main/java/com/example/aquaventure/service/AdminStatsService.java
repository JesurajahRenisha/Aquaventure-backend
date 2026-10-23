package com.example.aquaventure.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.admin.AdminStatsDto;
import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.repository.BookingRepository;
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.UserRepository;

@Service
public class AdminStatsService {

    private final UserRepository userRepository;
    private final SurfSchoolRepository surfSchoolRepository;
    private final BookingRepository bookingRepository;

    public AdminStatsService(UserRepository userRepository, SurfSchoolRepository surfSchoolRepository,
                              BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.surfSchoolRepository = surfSchoolRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public AdminStatsDto getStats() {
        AdminStatsDto dto = new AdminStatsDto();

        Map<String, Long> usersByRole = new LinkedHashMap<>();
        for (Role role : Role.values()) {
            usersByRole.put(role.name(), userRepository.countByRole(role));
        }
        dto.setUsersByRole(usersByRole);

        Map<String, Long> schoolsByStatus = new LinkedHashMap<>();
        for (SchoolStatus status : SchoolStatus.values()) {
            schoolsByStatus.put(status.name(), surfSchoolRepository.countByStatus(status));
        }
        dto.setSchoolsByStatus(schoolsByStatus);

        Map<String, Long> bookingsByStatus = new LinkedHashMap<>();
        for (BookingStatus status : BookingStatus.values()) {
            bookingsByStatus.put(status.name(), bookingRepository.countByStatus(status));
        }
        dto.setBookingsByStatus(bookingsByStatus);

        Map<String, Long> bookingsByLocation = new LinkedHashMap<>();
        for (Object[] row : bookingRepository.countGroupedByLocation()) {
            bookingsByLocation.put((String) row[0], (Long) row[1]);
        }
        dto.setBookingsByLocation(bookingsByLocation);

        return dto;
    }
}
