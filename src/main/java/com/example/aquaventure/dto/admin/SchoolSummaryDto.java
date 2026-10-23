package com.example.aquaventure.dto.admin;

import com.example.aquaventure.entity.SchoolStatus;

public class SchoolSummaryDto {

    private Long id;
    private String name;
    private String address;
    private SchoolStatus status;
    private String ownerEmail;

    public SchoolSummaryDto() {
    }

    public SchoolSummaryDto(Long id, String name, String address, SchoolStatus status, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.status = status;
        this.ownerEmail = ownerEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public SchoolStatus getStatus() {
        return status;
    }

    public void setStatus(SchoolStatus status) {
        this.status = status;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
