package com.example.aquaventure.service.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClient;

@SpringBootTest
@ActiveProfiles("test")
class SurfConditionCachingTest {

    @Autowired
    private SurfConditionProvider surfConditionProvider;

    @MockitoBean
    private RestClient restClient;

    @SuppressWarnings("unchecked")
    @Test
    void getConditions_secondCallWithSameCoordinates_hitsCacheNotHttpClient() {
        Map<String, Object> marine = Map.of("hourly", Map.of(
                "time", List.of("2026-11-01T00:00"),
                "wave_height", List.of(1.0),
                "wave_period", List.of(8.0),
                "wave_direction", List.of(100.0)));
        Map<String, Object> weather = Map.of("hourly", Map.of(
                "time", List.of("2026-11-01T00:00"),
                "temperature_2m", List.of(27.0),
                "windspeed_10m", List.of(10.0),
                "winddirection_10m", List.of(200.0)));

        RestClient.RequestHeadersUriSpec uriSpec = org.mockito.Mockito.mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec1 = org.mockito.Mockito.mock(RestClient.RequestHeadersSpec.class);
        RestClient.RequestHeadersSpec headersSpec2 = org.mockito.Mockito.mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec1 = org.mockito.Mockito.mock(RestClient.ResponseSpec.class);
        RestClient.ResponseSpec responseSpec2 = org.mockito.Mockito.mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(org.mockito.ArgumentMatchers.contains("/v1/marine"))).thenReturn(headersSpec1);
        when(uriSpec.uri(org.mockito.ArgumentMatchers.contains("/v1/forecast"))).thenReturn(headersSpec2);
        when(headersSpec1.retrieve()).thenReturn(responseSpec1);
        when(headersSpec2.retrieve()).thenReturn(responseSpec2);
        when(responseSpec1.body(Map.class)).thenReturn(marine);
        when(responseSpec2.body(Map.class)).thenReturn(weather);

        List<DailyCondition> first = surfConditionProvider.getConditions(9.9999, 10.0001);
        List<DailyCondition> second = surfConditionProvider.getConditions(9.9999, 10.0001);

        assertThat(first).isEqualTo(second);
        // Each getConditions() call makes 2 upstream calls (marine + weather); if the cache
        // worked, the second getConditions() call makes none, so the total stays at 2, not 4.
        verify(restClient, org.mockito.Mockito.times(2)).get();
    }
}
