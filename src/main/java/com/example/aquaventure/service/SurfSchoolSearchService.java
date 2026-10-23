package com.example.aquaventure.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.aquaventure.dto.discovery.SchoolCompareDto;
import com.example.aquaventure.dto.discovery.SchoolDetailPublicDto;
import com.example.aquaventure.dto.discovery.SchoolSummaryPublicDto;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.SchoolStatus;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfSchool;
import com.example.aquaventure.exception.ResourceNotFoundException;
import com.example.aquaventure.mapper.SchoolDiscoveryMapper;
import com.example.aquaventure.repository.EquipmentRepository;
import com.example.aquaventure.repository.InstructorRepository;
import com.example.aquaventure.repository.LessonRepository;
import com.example.aquaventure.repository.SurfSchoolRepository;
import com.example.aquaventure.repository.spec.SurfSchoolSpecifications;

@Service
public class SurfSchoolSearchService {

    private final SurfSchoolRepository surfSchoolRepository;
    private final LessonRepository lessonRepository;
    private final InstructorRepository instructorRepository;
    private final EquipmentRepository equipmentRepository;

    public SurfSchoolSearchService(SurfSchoolRepository surfSchoolRepository, LessonRepository lessonRepository,
                                    InstructorRepository instructorRepository, EquipmentRepository equipmentRepository) {
        this.surfSchoolRepository = surfSchoolRepository;
        this.lessonRepository = lessonRepository;
        this.instructorRepository = instructorRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<SchoolSummaryPublicDto> search(Long locationId, SkillLevel skillLevel, LessonType lessonType,
                                                BigDecimal minPrice, BigDecimal maxPrice, LocalDate date,
                                                String sort, Pageable pageable) {
        Specification<SurfSchool> spec = Specification.<SurfSchool>where(SurfSchoolSpecifications.isApproved())
                .and(SurfSchoolSpecifications.hasLocation(locationId))
                .and(SurfSchoolSpecifications.hasMatchingLesson(lessonType, skillLevel, minPrice, maxPrice))
                .and(SurfSchoolSpecifications.availableOn(date));

        if ("price".equalsIgnoreCase(sort)) {
            // Price isn't a direct column on SurfSchool (it lives on Lesson), so it can't be
            // pushed down into the DB-level Pageable sort via a Specification. The school
            // directory is small in practice, so we fetch all matches and sort/paginate in memory.
            List<SurfSchool> all = surfSchoolRepository.findAll(spec, Sort.by("name").ascending());
            List<SchoolSummaryPublicDto> dtos = all.stream()
                    .map(school -> SchoolDiscoveryMapper.toSummaryPublicDto(school, activeLessons(school.getId())))
                    .sorted(Comparator.comparing(SchoolSummaryPublicDto::getMinPrice,
                            Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();
            return paginate(dtos, pageable);
        }

        Sort effectiveSort = "name".equalsIgnoreCase(sort) || sort == null
                ? Sort.by("name").ascending()
                : Sort.by("name").ascending(); // "rating" has no backing data yet; fall back to name
        Pageable effectivePageable = pageable.getSort().isSorted() ? pageable
                : org.springframework.data.domain.PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), effectiveSort);

        Page<SurfSchool> page = surfSchoolRepository.findAll(spec, effectivePageable);
        return page.map(school -> SchoolDiscoveryMapper.toSummaryPublicDto(school, activeLessons(school.getId())));
    }

    @Transactional(readOnly = true)
    public SchoolDetailPublicDto getPublicDetail(Long id) {
        SurfSchool school = getApprovedOrThrow(id);
        return SchoolDiscoveryMapper.toDetailPublicDto(school, instructorRepository.findBySchoolIdAndActiveTrue(id),
                activeLessons(id), equipmentRepository.findBySchoolIdAndAvailableTrue(id));
    }

    @Transactional(readOnly = true)
    public List<SchoolCompareDto> compare(List<Long> ids) {
        return ids.stream()
                .map(surfSchoolRepository::findById)
                .flatMap(java.util.Optional::stream)
                .filter(school -> school.getStatus() == SchoolStatus.APPROVED)
                .map(school -> SchoolDiscoveryMapper.toCompareDto(school,
                        instructorRepository.findBySchoolIdAndActiveTrue(school.getId()),
                        activeLessons(school.getId()),
                        equipmentRepository.findBySchoolIdAndAvailableTrue(school.getId())))
                .toList();
    }

    private SurfSchool getApprovedOrThrow(Long id) {
        SurfSchool school = surfSchoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found: " + id));
        if (school.getStatus() != SchoolStatus.APPROVED) {
            throw new ResourceNotFoundException("School not found: " + id);
        }
        return school;
    }

    private List<Lesson> activeLessons(Long schoolId) {
        return lessonRepository.findBySchoolIdAndActiveTrue(schoolId);
    }

    private Page<SchoolSummaryPublicDto> paginate(List<SchoolSummaryPublicDto> all, Pageable pageable) {
        int start = (int) pageable.getOffset();
        if (start >= all.size()) {
            return new PageImpl<>(List.of(), pageable, all.size());
        }
        int end = Math.min(start + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(start, end), pageable, all.size());
    }
}
