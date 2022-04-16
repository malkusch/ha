package de.malkusch.ha.automation.application.heater;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.heater.TemporaryTemperatureService;
import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.ElectricityPredictionService;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.automation.model.heater.IncreaseHeaterRule;
import de.malkusch.ha.automation.model.heater.ResetHeaterRule;
import lombok.Data;

@Service
public final class SchedulHeaterRulesApplicationService {

    @ConfigurationProperties("buderus.heater.rules")
    @Component
    @Data
    public static class HeaterProperties {
        private Duration window;
        private Duration evaluationRate;

        private Increase increase;

        @Data
        public static class Increase {
            private int threshold;
            private double temperature;
        }

        private Reset reset;

        @Data
        public static class Reset {
            private int threshold;
        }
    }

    SchedulHeaterRulesApplicationService(DehumidifierRepository dehumidifiers, RuleScheduler scheduler,
            Electricity electricity, HeaterProperties properties,
            TemporaryTemperatureService temporaryTemperatureService,
            ElectricityPredictionService electricityPredictionService) {

        var evaluationRate = properties.evaluationRate;
        var window = properties.window;

        var increaseThreshold = new Watt(properties.increase.threshold);
        var increasedTemperature = new Temperature(properties.increase.temperature);
        var increase = new IncreaseHeaterRule(evaluationRate, window, increaseThreshold, increasedTemperature,
                temporaryTemperatureService, electricity, electricityPredictionService);
        scheduler.schedule(increase);

        var resetThreshold = new Watt(properties.reset.threshold);
        var reset = new ResetHeaterRule(evaluationRate, window, resetThreshold, temporaryTemperatureService,
                electricity);
        scheduler.schedule(reset);
    }
}
