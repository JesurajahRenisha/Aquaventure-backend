package com.example.aquaventure.service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.school.RevenueSummaryDto;
import com.example.aquaventure.dto.school.SchoolDashboardDto;
import com.example.aquaventure.entity.BookingStatus;
import com.example.aquaventure.entity.LessonSlotStatus;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.mapper.LessonSlotMapper;
import com.example.aquaventure.repository.BookingRepository;
import com.example.aquaventure.repository.LessonSlotRepository;

@Service
public class SchoolDashboardService {

    private static final int UPCOMING_LESSONS_LIMIT = 10;

    private final SurfSchoolService surfSchoolService;
    private final BookingRepository bookingRepository;
    private final LessonSlotRepository lessonSlotRepository;

    public SchoolDashboardService(SurfSchoolService surfSchoolService, BookingRepository bookingRepository,
                                   LessonSlotRepository lessonSlotRepository) {
        this.surfSchoolService = surfSchoolService;
        this.bookingRepository = bookingRepository;
        this.lessonSlotRepository = lessonSlotRepository;
    }

    @Transactional(readOnly = true)
    public SchoolDashboardDto getDashboard(Long ownerId) {
        SurfSchool school = surfSchoolService.getOwnedSchoolOrThrow(ownerId);

        SchoolDashboardDto dto = new SchoolDashboardDto();
        dto.setPendingBookingsCount(bookingRepository.countBySchoolIdAndStatus(school.getId(), BookingStatus.PENDING));
        dto.setUpcomingLessons(LessonSlotMapper.toDtoList(
                lessonSlotRepository.findByLessonSchoolIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                        school.getId(), LessonSlotStatus.OPEN, Instant.now(),
                        PageRequest.of(0, UPCOMING_LESSONS_LIMIT))));

        RevenueSummaryDto revenue = new RevenueSummaryDto();
        revenue.setTotalRevenue(bookingRepository.sumRevenueForSchool(school.getId()));
        revenue.setRevenueThisMonth(bookingRepository.sumRevenueForSchoolSince(school.getId(), startOfMonth()));
        revenue.setCompletedBookingsCount(bookingRepository.countBySchoolIdAndStatus(school.getId(), BookingStatus.COMPLETED));
        dto.setRevenue(revenue);

        return dto;
    }

    private Instant startOfMonth() {
        return Instant.now().atZone(ZoneOffset.UTC).with(ChronoField.DAY_OF_MONTH, 1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant();
    }
}
