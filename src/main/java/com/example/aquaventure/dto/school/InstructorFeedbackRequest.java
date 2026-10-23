package com.example.aquaventure.dto.school;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class InstructorFeedbackRequest {

    @NotBlank
    @Size(max = 2000)
    private String feedback;

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
