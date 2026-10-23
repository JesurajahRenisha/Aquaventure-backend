package com.example.aquaventure.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectSchoolRequest {

    @NotBlank(message = "A rejection reason is required")
    @Size(max = 1000)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
