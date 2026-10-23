package com.example.aquaventure.controller.discovery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.aquaventure.dto.common.ApiResponse;
import com.example.aquaventure.dto.common.PageResponse;
import com.example.aquaventure.dto.discovery.SchoolCompareDto;
import com.example.aquaventure.dto.discovery.SchoolDetailPublicDto;
import com.example.aquaventure.dto.discovery.SchoolSummaryPublicDto;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.service.SurfSchoolSearchService;

@RestController
@RequestMapping("/api/schools")
public class SchoolDiscoveryController {

    private final SurfSchoolSearchService surfSchoolSearchService;

    public SchoolDiscoveryController(SurfSchoolSearchService surfSchoolSearchService) {
        this.surfSchoolSearchService = surfSchoolSearchService;
    }

    @GetMapping
    public ApiResponse<PageResponse<SchoolSummaryPublicDto>> search(
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) SkillLevel skillLevel,
            @RequestParam(required = false) LessonType lessonType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String sort,
            Pageable pageable) {
        Page<SchoolSummaryPublicDto> page = surfSchoolSearchService.search(
                locationId, skillLevel, lessonType, minPrice, maxPrice, date, sort, pageable);
        return ApiResponse.ok(PageResponse.of(page));
    }

    @GetMapping("/compare")
    public ApiResponse<List<SchoolCompareDto>> compare(@RequestParam List<Long> ids) {
        if (ids.size() < 2 || ids.size() > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide between 2 and 5 school ids to compare");
        }
        return ApiResponse.ok(surfSchoolSearchService.compare(ids));
    }

    @GetMapping("/{id}")
    public ApiResponse<SchoolDetailPublicDto> getDetail(@PathVariable Long id) {
        return ApiResponse.ok(surfSchoolSearchService.getPublicDetail(id));
    }
}
