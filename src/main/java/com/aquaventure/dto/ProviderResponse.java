package com.aquaventure.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProviderResponse {
    private Long providerId;
    private Long userId;
    private String name;
    private String email;
    private String businessName;
    private String contactDetails;
    private String location;
}
