package com.example.aquaventure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.surf.suitability")
public class SurfSuitabilityProperties {

    private Threshold beginner = new Threshold();
    private Threshold intermediate = new Threshold();
    private Threshold advanced = new Threshold();

    public Threshold getBeginner() {
        return beginner;
    }

    public void setBeginner(Threshold beginner) {
        this.beginner = beginner;
    }

    public Threshold getIntermediate() {
        return intermediate;
    }

    public void setIntermediate(Threshold intermediate) {
        this.intermediate = intermediate;
    }

    public Threshold getAdvanced() {
        return advanced;
    }

    public void setAdvanced(Threshold advanced) {
        this.advanced = advanced;
    }

    public static class Threshold {
        private double maxWaveHeightM;
        private double maxWindSpeedKmh;

        public double getMaxWaveHeightM() {
            return maxWaveHeightM;
        }

        public void setMaxWaveHeightM(double maxWaveHeightM) {
            this.maxWaveHeightM = maxWaveHeightM;
        }

        public double getMaxWindSpeedKmh() {
            return maxWindSpeedKmh;
        }

        public void setMaxWindSpeedKmh(double maxWindSpeedKmh) {
            this.maxWindSpeedKmh = maxWindSpeedKmh;
        }
    }
}
