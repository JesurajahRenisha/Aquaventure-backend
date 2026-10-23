package com.example.aquaventure.controller.discovery;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.school.LessonSlotDto;
import com.example.aquaventure.service.LessonSlotPublicService;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonSlotPublicService lessonSlotPublicService;

    public LessonController(LessonSlotPublicService lessonSlotPublicService) {
        this.lessonSlotPublicService = lessonSlotPublicService;
    }

    @GetMapping("/{id}/slots")
    public ApiResponse<List<LessonSlotDto>> openSlots(@PathVariable Long id,
                                                        @RequestParam Instant from,
                                                        @RequestParam Instant to) {
        return ApiResponse.ok(lessonSlotPublicService.openSlots(id, from, to));
    }
}
