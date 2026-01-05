package de.malkusch.ha.automation.application.dehumidifier;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.climate.ClimateService;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.automation.model.dehumidifier.TurnOffDehumidifiersRule;
import de.malkusch.ha.automation.model.dehumidifier.TurnOnDehumidifiersRule;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public final class ScheduleDehumidiferRulesApplicationService {

    @ConfigurationProperties("dehumidifier.rules")
    @Component
    @Data
    public static class DehumidifierProperties {
        private int buffer;
        private Duration window;
        private Duration evaluationRate;
    }

    ScheduleDehumidiferRulesApplicationService(DehumidifierRepository dehumidifiers, RuleScheduler scheduler,
            Electricity electricity, DehumidifierProperties properties, ClimateService climateService) {

        var buffer = new Watt(properties.buffer);

        var turnOn = new TurnOnDehumidifiersRule(dehumidifiers, electricity, buffer, properties.window,
                properties.evaluationRate, climateService);
        scheduler.schedule(turnOn);

        var turnOff = new TurnOffDehumidifiersRule(dehumidifiers, electricity, buffer, properties.window,
                properties.evaluationRate, climateService);
        scheduler.schedule(turnOff);
    }
}
