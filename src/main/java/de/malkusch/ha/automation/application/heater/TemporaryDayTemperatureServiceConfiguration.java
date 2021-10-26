package de.malkusch.ha.automation.application.heater;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.automation.model.heater.Temperature;
import de.malkusch.ha.automation.model.heater.TemporaryDayTemperatureService;
import lombok.Data;

@Configuration
class TemporaryDayTemperatureServiceConfiguration {

    @ConfigurationProperties("buderus.heater.temporary-day-temperature")
    @Component
    @Data
    public static class Properties {
        private int steps;
        private double step;
    }

    @Bean
    public TemporaryDayTemperatureService temporaryDayTemperatureService(Heater heater, Properties properties) {
        var step = new Temperature(properties.step);
        return new TemporaryDayTemperatureService(heater, properties.steps, step);
    }
}
