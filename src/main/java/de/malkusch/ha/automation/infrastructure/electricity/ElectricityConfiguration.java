package de.malkusch.ha.automation.infrastructure.electricity;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.ElectricityPredictionService;
import de.malkusch.ha.automation.model.electricity.ElectricityPredictionService.SolarIrradianceForecast;
import de.malkusch.ha.automation.model.electricity.Wallbox;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.shared.model.ApiException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ElectricityConfiguration {

    private final ElectricityProperties properties;

    @ConfigurationProperties("electricity")
    @Component
    @Data
    public static class ElectricityProperties {

        private Prediction prediction;

        @Data
        public static class Prediction {
            private double minimumPeak;
            private Duration peakWindow;
            private Duration learningWindow;
        }

        private Wallbox wallbox;

        @Data
        public static class Wallbox {
            private double minimumLoadingConsumption;
            private Duration minimumLoadingDuration;
        }

    }

    @Bean
    ElectricityPredictionService electricityPredictionService(Electricity electricity,
            SolarIrradianceForecast irradiationForecast, Wallbox wallbox) throws ApiException, InterruptedException {

        var minimumPeak = new Watt(properties.prediction.minimumPeak);
        var peakWindow = properties.prediction.peakWindow;
        var learningWindow = properties.prediction.learningWindow;

        return new ElectricityPredictionService(electricity, minimumPeak, peakWindow, irradiationForecast,
                learningWindow, wallbox);
    }

    @Bean
    Wallbox wallbox(Electricity electricity) {
        var properties = this.properties.wallbox;
        var minimumLoadingConsumption = new Watt(properties.minimumLoadingConsumption);
        var minimumLoadingDuration = properties.minimumLoadingDuration;
        return new Wallbox(minimumLoadingConsumption, minimumLoadingDuration, electricity);
    }
}
