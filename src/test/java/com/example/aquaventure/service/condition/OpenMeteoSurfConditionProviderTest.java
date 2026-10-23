package com.example.aquaventure.service.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.example.aquaventure.exception.ExternalServiceUnavailableException;

class OpenMeteoSurfConditionProviderTest {

    private RestClient restClient;
    private OpenMeteoSurfConditionProvider provider;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class, Answers.RETURNS_DEEP_STUBS);
        provider = new OpenMeteoSurfConditionProvider(restClient);
    }

    private Map<String, Object> marineResponse() {
        return Map.of("hourly", Map.of(
                "time", List.of("2026-11-01T00:00", "2026-11-01T06:00", "2026-11-02T00:00"),
                "wave_height", List.of(0.8, 1.2, 1.5),
                "wave_period", List.of(8.0, 9.0, 10.0),
                "wave_direction", List.of(100.0, 110.0, 120.0)));
    }

    private Map<String, Object> weatherResponse() {
        return Map.of("hourly", Map.of(
                "time", List.of("2026-11-01T00:00", "2026-11-01T06:00", "2026-11-02T00:00"),
                "temperature_2m", List.of(26.0, 28.0, 27.0),
                "windspeed_10m", List.of(10.0, 15.0, 20.0),
                "winddirection_10m", List.of(200.0, 210.0, 220.0)));
    }

    private void stubResponses(Map<String, Object> first, Map<String, Object> second) {
        // "marine-api.open-meteo.com" contains "api.open-meteo.com" as a substring, so the two
        // matchers must key off the distinct path segments ("/v1/marine" vs "/v1/forecast"),
        // not the host alone, or Mockito's last-registered-matcher-wins rule picks the wrong one.
        when(restClient.get().uri(contains("/v1/marine")).retrieve().body(Map.class)).thenReturn(first);
        when(restClient.get().uri(contains("/v1/forecast")).retrieve().body(Map.class)).thenReturn(second);
    }

    @Test
    void getConditions_happyPath_mergesMarineAndWeatherByDate() {
        stubResponses(marineResponse(), weatherResponse());

        List<DailyCondition> result = provider.getConditions(6.84, 81.83);

        assertThat(result).hasSize(2);
        DailyCondition day1 = result.get(0);
        assertThat(day1.maxWaveHeightM()).isEqualTo(1.2);
        assertThat(day1.avgWindSpeedKmh()).isEqualTo(12.5);
    }

    @Test
    void getConditions_upstreamTimeout_throwsExternalServiceUnavailable() {
        when(restClient.get().uri(any(String.class)).retrieve().body(Map.class))
                .thenThrow(new RestClientException("timeout"));

        assertThatThrownBy(() -> provider.getConditions(6.84, 81.83))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void getConditions_malformedResponse_throwsExternalServiceUnavailable() {
        when(restClient.get().uri(any(String.class)).retrieve().body(Map.class))
                .thenReturn(Map.of("unexpected", "shape"));

        assertThatThrownBy(() -> provider.getConditions(6.84, 81.83))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }

    @Test
    void getConditions_nullResponse_throwsExternalServiceUnavailable() {
        when(restClient.get().uri(any(String.class)).retrieve().body(Map.class)).thenReturn(null);

        assertThatThrownBy(() -> provider.getConditions(6.84, 81.83))
                .isInstanceOf(ExternalServiceUnavailableException.class);
    }
}
