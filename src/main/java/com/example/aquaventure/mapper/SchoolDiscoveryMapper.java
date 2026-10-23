package com.example.aquaventure.mapper;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.aquaventure.dto.discovery.SchoolCompareDto;
import com.example.aquaventure.dto.discovery.SchoolDetailPublicDto;
import com.example.aquaventure.dto.discovery.SchoolSummaryPublicDto;
import com.example.aquaventure.entity.Equipment;
import com.example.aquaventure.entity.Instructor;
import com.example.aquaventure.entity.Lesson;
import com.example.aquaventure.entity.LessonType;
import com.example.aquaventure.entity.SkillLevel;
import com.example.aquaventure.entity.SurfLocation;
import com.example.aquaventure.entity.SurfSchool;

public final class SchoolDiscoveryMapper {

    private SchoolDiscoveryMapper() {
    }

    public static SchoolSummaryPublicDto toSummaryPublicDto(SurfSchool school, List<Lesson> activeLessons) {
        SchoolSummaryPublicDto dto = new SchoolSummaryPublicDto();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setDescription(school.getDescription());
        dto.setAddress(school.getAddress());
        dto.setLogoUrl(school.getLogoUrl());
        dto.setLocationNames(school.getLocations().stream().map(SurfLocation::getName).toList());
        dto.setLessonTypesOffered(activeLessons.stream().map(Lesson::getLessonType).collect(java.util.stream.Collectors.toSet()));
        activeLessons.stream().min((a, b) -> a.getPrice().compareTo(b.getPrice()))
                .ifPresent(l -> {
                    dto.setMinPrice(l.getPrice());
                    dto.setCurrency(l.getCurrency());
                });
        activeLessons.stream().max((a, b) -> a.getPrice().compareTo(b.getPrice()))
                .ifPresent(l -> dto.setMaxPrice(l.getPrice()));
        return dto;
    }

    public static SchoolDetailPublicDto toDetailPublicDto(SurfSchool school, List<Instructor> instructors,
                                                            List<Lesson> lessons, List<Equipment> equipment) {
        SchoolDetailPublicDto dto = new SchoolDetailPublicDto();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setDescription(school.getDescription());
        dto.setAddress(school.getAddress());
        dto.setContactPhone(school.getContactPhone());
        dto.setContactEmail(school.getContactEmail());
        dto.setLogoUrl(school.getLogoUrl());
        dto.setImageUrls(new java.util.ArrayList<>(school.getImageUrls()));
        dto.setLocationNames(school.getLocations().stream().map(SurfLocation::getName).toList());
        dto.setInstructors(InstructorMapper.toDtoList(instructors));
        dto.setLessons(LessonMapper.toDtoList(lessons));
        dto.setEquipment(EquipmentMapper.toDtoList(equipment));
        return dto;
    }

    public static SchoolCompareDto toCompareDto(SurfSchool school, List<Instructor> instructors, List<Lesson> lessons,
                                                 List<Equipment> equipment) {
        SchoolCompareDto dto = new SchoolCompareDto();
        dto.setId(school.getId());
        dto.setName(school.getName());
        dto.setLocationNames(school.getLocations().stream().map(SurfLocation::getName).toList());
        dto.setInstructorCount(instructors.size());

        Set<LessonType> lessonTypes = new HashSet<>();
        Set<SkillLevel> skillLevels = new HashSet<>();
        BigDecimal min = null;
        BigDecimal max = null;
        for (Lesson lesson : lessons) {
            lessonTypes.add(lesson.getLessonType());
            skillLevels.add(lesson.getSkillLevel());
            if (min == null || lesson.getPrice().compareTo(min) < 0) {
                min = lesson.getPrice();
                dto.setCurrency(lesson.getCurrency());
            }
            if (max == null || lesson.getPrice().compareTo(max) > 0) {
                max = lesson.getPrice();
            }
        }
        dto.setLessonTypesOffered(lessonTypes);
        dto.setSkillLevelsSupported(skillLevels);
        dto.setMinPrice(min);
        dto.setMaxPrice(max);

        Set<String> languages = new HashSet<>();
        for (Instructor instructor : instructors) {
            languages.addAll(instructor.getLanguages());
        }
        dto.setLanguagesOffered(languages);
        dto.setEquipmentRentalAvailable(!equipment.isEmpty());
        return dto;
    }
}
