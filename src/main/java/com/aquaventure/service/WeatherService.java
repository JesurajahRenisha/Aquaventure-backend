package com.aquaventure.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aquaventure.dto.WeatherResponse;
import com.aquaventure.entity.SurfLocation;
import com.aquaventure.entity.WeatherInformation;
import com.aquaventure.repository.WeatherInformationRepository;
import com.aquaventure.service.weather.WeatherClient;
import com.aquaventure.service.weather.WeatherReading;

@Service
@Transactional
public class WeatherService {

    @Autowired
    private WeatherInformationRepository weatherInformationRepository;

    @Autowired
    private SurfLocationService surfLocationService;

    @Autowired
    private WeatherClient weatherClient;

    @Value("${weather.cache-ttl-minutes:60}")
    private long cacheTtlMinutes;

    public WeatherResponse getForLocation(Long locationId) {
        SurfLocation location = surfLocationService.getEntity(locationId);
        Optional<WeatherInformation> cached = weatherInformationRepository.findFirstByLocationOrderByRecordedAtDesc(location);

        if (cached.isPresent() && isFresh(cached.get())) {
            return toResponse(cached.get(), true);
        }

        WeatherReading reading = weatherClient.fetchCurrentConditions();
        WeatherInformation info = new WeatherInformation();
        info.setLocation(location);
        info.setWaveHeight(reading.waveHeight());
        info.setWindSpeed(reading.windSpeed());
        info.setTemperature(reading.temperature());
        info = weatherInformationRepository.save(info);

        return toResponse(info, false);
    }

    private boolean isFresh(WeatherInformation info) {
        return Duration.between(info.getRecordedAt(), Instant.now()).toMinutes() < cacheTtlMinutes;
    }

    private WeatherResponse toResponse(WeatherInformation info, boolean fromCache) {
        return new WeatherResponse(
                info.getWeatherId(),
                info.getLocation().getLocationId(),
                info.getWaveHeight(),
                info.getWindSpeed(),
                info.getTemperature(),
                info.getRecordedAt(),
                fromCache);
    }
}
