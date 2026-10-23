package com.example.aquaventure.service.condition;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.aquaventure.config.CacheConfig;
import com.example.aquaventure.exception.ExternalServiceUnavailableException;

@Service
public class OpenMeteoSurfConditionProvider implements SurfConditionProvider {

    private static final String MARINE_URL = "https://marine-api.open-meteo.com/v1/marine";
    private static final String WEATHER_URL = "https://api.open-meteo.com/v1/forecast";
    private static final int FORECAST_DAYS = 4; // today + next 3 days

    private final RestClient restClient;

    public OpenMeteoSurfConditionProvider(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Cacheable(value = CacheConfig.SURF_CONDITIONS_CACHE, key = "#latitude + ':' + #longitude")
    public List<DailyCondition> getConditions(double latitude, double longitude) {
        try {
            Map<String, Object> marine = fetch(MARINE_URL, latitude, longitude,
                    "wave_height,wave_period,wave_direction");
            Map<String, Object> weather = fetch(WEATHER_URL, latitude, longitude,
                    "temperature_2m,windspeed_10m,winddirection_10m");
            return merge(marine, weather);
        } catch (RestClientException | ClassCastException | NullPointerException e) {
            throw new ExternalServiceUnavailableException("Surf conditions are temporarily unavailable", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetch(String baseUrl, double latitude, double longitude, String hourlyParams) {
        String uri = baseUrl + "?latitude=" + latitude + "&longitude=" + longitude
                + "&hourly=" + hourlyParams + "&timezone=auto&forecast_days=" + FORECAST_DAYS;
        Map<String, Object> response = restClient.get().uri(uri).retrieve().body(Map.class);
        if (response == null || !(response.get("hourly") instanceof Map)) {
            throw new ExternalServiceUnavailableException("Unexpected response from surf conditions provider");
        }
        return (Map<String, Object>) response.get("hourly");
    }

    @SuppressWarnings("unchecked")
    private List<DailyCondition> merge(Map<String, Object> marineHourly, Map<String, Object> weatherHourly) {
        List<String> times = (List<String>) marineHourly.get("time");
        List<Number> waveHeight = (List<Number>) marineHourly.get("wave_height");
        List<Number> wavePeriod = (List<Number>) marineHourly.get("wave_period");
        List<Number> waveDirection = (List<Number>) marineHourly.get("wave_direction");
        List<Number> temperature = (List<Number>) weatherHourly.get("temperature_2m");
        List<Number> windSpeed = (List<Number>) weatherHourly.get("windspeed_10m");
        List<Number> windDirection = (List<Number>) weatherHourly.get("winddirection_10m");

        Map<LocalDate, List<Integer>> indexesByDate = new LinkedHashMap<>();
        for (int i = 0; i < times.size(); i++) {
            LocalDate date = LocalDate.parse(times.get(i).substring(0, 10));
            indexesByDate.computeIfAbsent(date, d -> new ArrayList<>()).add(i);
        }

        List<DailyCondition> result = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Integer>> entry : indexesByDate.entrySet()) {
            if (result.size() >= FORECAST_DAYS) {
                break;
            }
            List<Integer> idx = entry.getValue();
            result.add(new DailyCondition(
                    entry.getKey(),
                    max(waveHeight, idx),
                    avg(wavePeriod, idx),
                    avg(waveDirection, idx),
                    avg(windSpeed, idx),
                    avg(windDirection, idx),
                    avg(temperature, idx)));
        }
        return result;
    }

    private double max(List<Number> values, List<Integer> indexes) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i : indexes) {
            max = Math.max(max, values.get(i).doubleValue());
        }
        return max;
    }

    private double avg(List<Number> values, List<Integer> indexes) {
        double sum = 0;
        for (int i : indexes) {
            sum += values.get(i).doubleValue();
        }
        return sum / indexes.size();
    }
}
