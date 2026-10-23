package com.example.aquaventure.config;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.aquaventure.dto.auth.RegisterSchoolRequest;
import com.example.aquaventure.dto.auth.RegisterSurferRequest;
import com.example.aquaventure.dto.booking.CreateBookingRequest;
import com.example.aquaventure.dto.school.UpsertEquipmentRequest;
import com.example.aquaventure.dto.school.UpsertInstructorRequest;
import com.example.aquaventure.dto.school.UpsertLessonRequest;
import com.example.aquaventure.dto.school.UpsertLessonSlotRequest;
import com.example.aquaventure.entity.Currency;
import com.example.aquaventure.entity.EquipmentType;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.entity.User;
import com.example.aquaventure.repository.SurfLocationRepository;
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.UserRepository;
import com.example.aquaventure.service.AuthService;
import com.example.aquaventure.service.BookingService;
import com.example.aquaventure.service.EquipmentService;
import com.example.aquaventure.service.InstructorService;
import com.example.aquaventure.service.LessonService;
import com.example.aquaventure.service.LessonSlotService;
import com.example.aquaventure.service.SurfSchoolService;

/** Dev-only convenience data so the frontend has something to render locally. Idempotent. */
@Component
@Profile("dev")
public class DevDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);
    private static final String MARKER_SCHOOL_EMAIL = "seed.arugam.owner@aquaventure.dev";

    private final UserRepository userRepository;
    private final SurfSchoolRepository surfSchoolRepository;
    private final SurfLocationRepository surfLocationRepository;
    private final AuthService authService;
    private final SurfSchoolService surfSchoolService;
    private final InstructorService instructorService;
    private final LessonService lessonService;
    private final LessonSlotService lessonSlotService;
    private final EquipmentService equipmentService;
    private final BookingService bookingService;

    public DevDataSeeder(UserRepository userRepository, SurfSchoolRepository surfSchoolRepository,
                          SurfLocationRepository surfLocationRepository, AuthService authService,
                          SurfSchoolService surfSchoolService, InstructorService instructorService,
                          LessonService lessonService, LessonSlotService lessonSlotService,
                          EquipmentService equipmentService, BookingService bookingService) {
        this.userRepository = userRepository;
        this.surfSchoolRepository = surfSchoolRepository;
        this.surfLocationRepository = surfLocationRepository;
        this.authService = authService;
        this.surfSchoolService = surfSchoolService;
        this.instructorService = instructorService;
        this.lessonService = lessonService;
        this.lessonSlotService = lessonSlotService;
        this.equipmentService = equipmentService;
        this.bookingService = bookingService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(MARKER_SCHOOL_EMAIL)) {
            log.info("Dev seed data already present - skipping");
            return;
        }

        User seedAdmin = seedAdmin();
        List<Long> locationIds = surfLocationRepository.findByActiveTrue().stream()
                .map(com.example.aquaventure.entity.SurfLocation::getId).toList();
        Long arugamBayId = locationIds.isEmpty() ? null : locationIds.get(0);

        Long approvedSchool1Owner = seedApprovedSchool("Arugam Surf Academy", MARKER_SCHOOL_EMAIL, seedAdmin, arugamBayId);
        seedApprovedSchool("Whisky Point Surf Co", "seed.whisky.owner@aquaventure.dev", seedAdmin, arugamBayId);
        seedPendingSchool("Pottuvil Point Surf", "seed.pottuvil.owner@aquaventure.dev");

        seedSurferWithBookings(approvedSchool1Owner);

        log.info("Dev seed data created successfully");
    }

    private User seedAdmin() {
        return userRepository.findByEmail("seed.platform.admin@aquaventure.dev").orElseGet(() -> {
            User admin = new User();
            admin.setFirstname("Seed");
            admin.setLastname("Admin");
            admin.setEmail("seed.platform.admin@aquaventure.dev");
            admin.setPassword("$2a$10$seedseedseedseedseedseedseedseedseedseedseedseedse");
            admin.setRole(Role.PLATFORM_ADMIN);
            return userRepository.save(admin);
        });
    }

    private Long seedApprovedSchool(String name, String ownerEmail, User admin, Long locationId) {
        RegisterSchoolRequest request = new RegisterSchoolRequest();
        request.setOwnerFirstname("Seed");
        request.setOwnerLastname("Owner");
        request.setEmail(ownerEmail);
        request.setPassword("Password1!");
        request.setSchoolName(name);
        request.setBusinessRegistrationNumber("SEED-" + name.hashCode());
        if (locationId != null) {
            request.setLocationIds(List.of(locationId));
        }
        SurfSchool school = authService.registerSchool(request, null);
        surfSchoolService.approve(school.getId(), admin);

        Long ownerId = school.getOwner().getId();
        Long instructorId = createInstructor(ownerId);
        Long lessonId = createLesson(ownerId, locationId);
        createEquipment(ownerId);
        createSlotsForNext14Days(ownerId, lessonId, instructorId);
        return ownerId;
    }

    private void seedPendingSchool(String name, String ownerEmail) {
        RegisterSchoolRequest request = new RegisterSchoolRequest();
        request.setOwnerFirstname("Seed");
        request.setOwnerLastname("Pending");
        request.setEmail(ownerEmail);
        request.setPassword("Password1!");
        request.setSchoolName(name);
        request.setBusinessRegistrationNumber("SEED-" + name.hashCode());
        authService.registerSchool(request, null);
    }

    private Long createInstructor(Long ownerId) {
        UpsertInstructorRequest request = new UpsertInstructorRequest();
        request.setName("Seed Instructor");
        request.setBio("Experienced local instructor");
        request.setExperienceYears(8);
        request.setCertifications(List.of("ISA Level 2"));
        request.setLanguages(List.of("English", "Sinhala"));
        request.setSkillLevelsTaught(Set.of(SkillLevel.BEGINNER, SkillLevel.INTERMEDIATE));
        return instructorService.create(ownerId, request).getId();
    }

    private Long createLesson(Long ownerId, Long locationId) {
        UpsertLessonRequest request = new UpsertLessonRequest();
        request.setTitle("Beginner Group Surf Lesson");
        request.setDescription("A fun introduction to surfing for beginners");
        request.setLessonType(LessonType.GROUP);
        request.setSkillLevel(SkillLevel.BEGINNER);
        request.setDurationMinutes(90);
        request.setPrice(java.math.BigDecimal.valueOf(45));
        request.setCurrency(Currency.USD);
        request.setMaxParticipants(6);
        request.setLocationId(locationId);
        return lessonService.create(ownerId, request).getId();
    }

    private void createEquipment(Long ownerId) {
        UpsertEquipmentRequest request = new UpsertEquipmentRequest();
        request.setType(EquipmentType.SOFTBOARD);
        request.setName("Seed Softboard 8ft");
        request.setTotalQuantity(8);
        request.setRentalPricePerHour(java.math.BigDecimal.valueOf(5));
        request.setRentalPricePerDay(java.math.BigDecimal.valueOf(20));
        equipmentService.create(ownerId, request);
    }

    private void createSlotsForNext14Days(Long ownerId, Long lessonId, Long instructorId) {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.HOURS);
        for (int day = 0; day < 14; day++) {
            UpsertLessonSlotRequest request = new UpsertLessonSlotRequest();
            request.setLessonId(lessonId);
            request.setInstructorId(instructorId);
            Instant slotStart = start.plus(day, ChronoUnit.DAYS);
            request.setStartTime(slotStart);
            request.setEndTime(slotStart.plus(90, ChronoUnit.MINUTES));
            request.setCapacity(6);
            lessonSlotService.create(ownerId, request);
        }
    }

    private void seedSurferWithBookings(Long school1OwnerId) {
        RegisterSurferRequest surferRequest = new RegisterSurferRequest();
        surferRequest.setFirstname("Seed");
        surferRequest.setLastname("Surfer");
        surferRequest.setEmail("seed.surfer@aquaventure.dev");
        surferRequest.setPassword("Password1!");
        User surfer = authService.registerSurfer(surferRequest);

        SurfSchool school1 = surfSchoolRepository.findByOwnerId(school1OwnerId).orElse(null);
        if (school1 == null) {
            return;
        }

        var slotDtos = lessonSlotService.list(school1OwnerId, org.springframework.data.domain.PageRequest.of(0, 20)).getContent();
        if (slotDtos.size() < 2) {
            return;
        }

        CreateBookingRequest completedBookingRequest = new CreateBookingRequest();
        completedBookingRequest.setLessonSlotId(slotDtos.get(0).getId());
        completedBookingRequest.setParticipants(1);
        completedBookingRequest.setNote("Seed completed booking");
        Long completedBookingId = bookingService.create(surfer.getId(), completedBookingRequest).getId();
        bookingService.accept(school1OwnerId, completedBookingId);
        bookingService.complete(school1OwnerId, completedBookingId);

        CreateBookingRequest pendingBookingRequest = new CreateBookingRequest();
        pendingBookingRequest.setLessonSlotId(slotDtos.get(1).getId());
        pendingBookingRequest.setParticipants(2);
        pendingBookingRequest.setNote("Seed pending booking");
        bookingService.create(surfer.getId(), pendingBookingRequest);
    }
}
