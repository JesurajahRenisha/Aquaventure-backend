package com.example.aquaventure.controller.school;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.common.PageResponse;
import com.example.aquaventure.dto.school.EquipmentDto;
import com.example.aquaventure.dto.school.InstructorDto;
import com.example.aquaventure.dto.school.LessonDto;
import com.example.aquaventure.dto.school.LessonSlotDto;
import com.example.aquaventure.dto.school.SchoolProfileDto;
import com.example.aquaventure.dto.school.UpdateSchoolProfileRequest;
import com.example.aquaventure.dto.school.UpsertEquipmentRequest;
import com.example.aquaventure.dto.school.UpsertInstructorRequest;
import com.example.aquaventure.dto.school.UpsertLessonRequest;
import com.example.aquaventure.dto.school.UpsertLessonSlotRequest;
import com.example.aquaventure.security.UserPrincipal;
import com.example.aquaventure.service.EquipmentService;
import com.example.aquaventure.service.InstructorService;
import com.example.aquaventure.service.LessonService;
import com.example.aquaventure.service.LessonSlotService;
import com.example.aquaventure.service.SchoolProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/school/me")
@PreAuthorize("hasRole('SCHOOL_ADMIN')")
public class SchoolAdminController {

    private final SchoolProfileService schoolProfileService;
    private final InstructorService instructorService;
    private final LessonService lessonService;
    private final LessonSlotService lessonSlotService;
    private final EquipmentService equipmentService;

    public SchoolAdminController(SchoolProfileService schoolProfileService, InstructorService instructorService,
                                  LessonService lessonService, LessonSlotService lessonSlotService,
                                  EquipmentService equipmentService) {
        this.schoolProfileService = schoolProfileService;
        this.instructorService = instructorService;
        this.lessonService = lessonService;
        this.lessonSlotService = lessonSlotService;
        this.equipmentService = equipmentService;
    }

    // --- profile (allowed while PENDING) ---

    @GetMapping
    public ApiResponse<SchoolProfileDto> getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(schoolProfileService.getOwnProfile(principal.getId()));
    }

    @PutMapping
    public ApiResponse<SchoolProfileDto> updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody UpdateSchoolProfileRequest request) {
        return ApiResponse.ok(schoolProfileService.updateOwnProfile(principal.getId(), request));
    }

    // --- instructors (require APPROVED) ---

    @GetMapping("/instructors")
    public ApiResponse<PageResponse<InstructorDto>> listInstructors(@AuthenticationPrincipal UserPrincipal principal,
                                                                      Pageable pageable) {
        Page<InstructorDto> page = instructorService.list(principal.getId(), pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @PostMapping("/instructors")
    public ResponseEntity<ApiResponse<InstructorDto>> createInstructor(@AuthenticationPrincipal UserPrincipal principal,
                                                                        @Valid @RequestBody UpsertInstructorRequest request) {
        InstructorDto dto = instructorService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }

    @PutMapping("/instructors/{id}")
    public ApiResponse<InstructorDto> updateInstructor(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody UpsertInstructorRequest request) {
        return ApiResponse.ok(instructorService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/instructors/{id}")
    public ApiResponse<Void> deleteInstructor(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        instructorService.delete(principal.getId(), id);
        return ApiResponse.ok(null);
    }

    // --- lessons (require APPROVED) ---

    @GetMapping("/lessons")
    public ApiResponse<PageResponse<LessonDto>> listLessons(@AuthenticationPrincipal UserPrincipal principal,
                                                              Pageable pageable) {
        Page<LessonDto> page = lessonService.list(principal.getId(), pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @PostMapping("/lessons")
    public ResponseEntity<ApiResponse<LessonDto>> createLesson(@AuthenticationPrincipal UserPrincipal principal,
                                                                 @Valid @RequestBody UpsertLessonRequest request) {
        LessonDto dto = lessonService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }

    @PutMapping("/lessons/{id}")
    public ApiResponse<LessonDto> updateLesson(@AuthenticationPrincipal UserPrincipal principal,
                                                @PathVariable Long id,
                                                @Valid @RequestBody UpsertLessonRequest request) {
        return ApiResponse.ok(lessonService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/lessons/{id}")
    public ApiResponse<Void> deleteLesson(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        lessonService.delete(principal.getId(), id);
        return ApiResponse.ok(null);
    }

    // --- slots (require APPROVED) ---

    @GetMapping("/slots")
    public ApiResponse<PageResponse<LessonSlotDto>> listSlots(@AuthenticationPrincipal UserPrincipal principal,
                                                                Pageable pageable) {
        Page<LessonSlotDto> page = lessonSlotService.list(principal.getId(), pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @PostMapping("/slots")
    public ResponseEntity<ApiResponse<LessonSlotDto>> createSlot(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @Valid @RequestBody UpsertLessonSlotRequest request) {
        LessonSlotDto dto = lessonSlotService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }

    @PutMapping("/slots/{id}")
    public ApiResponse<LessonSlotDto> updateSlot(@AuthenticationPrincipal UserPrincipal principal,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody UpsertLessonSlotRequest request) {
        return ApiResponse.ok(lessonSlotService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/slots/{id}")
    public ApiResponse<Void> deleteSlot(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        lessonSlotService.delete(principal.getId(), id);
        return ApiResponse.ok(null);
    }

    // --- equipment (require APPROVED) ---

    @GetMapping("/equipment")
    public ApiResponse<PageResponse<EquipmentDto>> listEquipment(@AuthenticationPrincipal UserPrincipal principal,
                                                                   Pageable pageable) {
        Page<EquipmentDto> page = equipmentService.list(principal.getId(), pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @PostMapping("/equipment")
    public ResponseEntity<ApiResponse<EquipmentDto>> createEquipment(@AuthenticationPrincipal UserPrincipal principal,
                                                                      @Valid @RequestBody UpsertEquipmentRequest request) {
        EquipmentDto dto = equipmentService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(dto));
    }

    @PutMapping("/equipment/{id}")
    public ApiResponse<EquipmentDto> updateEquipment(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable Long id,
                                                       @Valid @RequestBody UpsertEquipmentRequest request) {
        return ApiResponse.ok(equipmentService.update(principal.getId(), id, request));
    }

    @DeleteMapping("/equipment/{id}")
    public ApiResponse<Void> deleteEquipment(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        equipmentService.delete(principal.getId(), id);
        return ApiResponse.ok(null);
    }
}
