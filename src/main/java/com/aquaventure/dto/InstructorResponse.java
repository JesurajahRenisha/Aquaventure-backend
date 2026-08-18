package com.aquaventure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class InstructorResponse {
    private Long instructorId;
    private Long providerId;
    private String name;
    private String certification;
    private String experience;
    private boolean availability;
}
