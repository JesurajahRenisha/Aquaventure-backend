package com.aquaventure.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.aquaventure.dto.WeatherResponse;
import com.aquaventure.service.WeatherService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/location/{locationId}")
    public WeatherResponse getForLocation(@PathVariable Long locationId) {
        return weatherService.getForLocation(locationId);
    }
}
