package com.example.aquaventure.dto.admin;

import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.UserStatus;

public class UserSummaryDto {

    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private Role role;
    private UserStatus status;
    private String phone;
    private String nationality;

    public UserSummaryDto() {
    }

    public UserSummaryDto(Long id, String firstname, String lastname, String email, Role role,
                           UserStatus status, String phone, String nationality) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.role = role;
        this.status = status;
        this.phone = phone;
        this.nationality = nationality;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }
}
