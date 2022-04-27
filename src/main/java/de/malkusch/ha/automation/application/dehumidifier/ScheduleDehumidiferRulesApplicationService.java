package de.malkusch.ha.automation.application.dehumidifier;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.Percent;
import de.malkusch.ha.automation.model.climate.ClimateService;
import de.malkusch.ha.automation.model.climate.Humidity;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.automation.model.dehumidifier.TurnOffDehumidifiersRule;
import de.malkusch.ha.automation.model.dehumidifier.TurnOnDehumidifiersRule;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import lombok.Data;

@Service
public final class ScheduleDehumidiferRulesApplicationService {

    @ConfigurationProperties("dehumidifier.rules")
    @Component
    @Data
    public static class DehumidifierProperties {
        private int buffer;
        private Duration window;
        private Duration evaluationRate;
        private Humidity humidity;

        @Data
        public static class Humidity {
            private double minimum;
            private double maximum;
        }
    }

    ScheduleDehumidiferRulesApplicationService(DehumidifierRepository dehumidifiers, RuleScheduler scheduler,
            Electricity electricity, DehumidifierProperties properties, ClimateService climateService) {

        var buffer = new Watt(properties.buffer);
        var minimumHumidity = new Humidity(new Percent(properties.humidity.minimum));
        var maximumHumidity = new Humidity(new Percent(properties.humidity.maximum));

        var turnOn = new TurnOnDehumidifiersRule(dehumidifiers, electricity, buffer, properties.window,
                properties.evaluationRate, climateService, maximumHumidity);
        scheduler.schedule(turnOn);

        var turnOff = new TurnOffDehumidifiersRule(dehumidifiers, electricity, buffer, properties.window,
                properties.evaluationRate, climateService, minimumHumidity);
        scheduler.schedule(turnOff);
    }
}
