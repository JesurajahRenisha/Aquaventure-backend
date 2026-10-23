package com.example.aquaventure.dto.auth;

import com.example.aquaventure.entity.Role;
import com.example.aquaventure.entity.SchoolStatus;

public class LoginResponse {

    private String token;
    private Long userId;
    private String email;
    private String firstname;
    private String lastname;
    private Role role;
    private Long schoolId;
    private SchoolStatus schoolStatus;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long userId, String email, String firstname, String lastname, Role role) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.firstname = firstname;
        this.lastname = lastname;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public SchoolStatus getSchoolStatus() {
        return schoolStatus;
    }

    public void setSchoolStatus(SchoolStatus schoolStatus) {
        this.schoolStatus = schoolStatus;
    }
}
