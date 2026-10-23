package com.example.aquaventure.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SurfSuitabilityPropertiesTest {

    @Autowired
    private SurfSuitabilityProperties properties;

    @Test
    void thresholds_bindFromApplicationProperties() {
        assertThat(properties.getBeginner().getMaxWaveHeightM()).isEqualTo(1.2);
        assertThat(properties.getBeginner().getMaxWindSpeedKmh()).isEqualTo(20);
        assertThat(properties.getIntermediate().getMaxWaveHeightM()).isEqualTo(2.0);
        assertThat(properties.getIntermediate().getMaxWindSpeedKmh()).isEqualTo(30);
        assertThat(properties.getAdvanced().getMaxWaveHeightM()).isEqualTo(99);
    }
}
