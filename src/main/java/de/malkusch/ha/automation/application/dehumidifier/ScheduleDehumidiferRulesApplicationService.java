package de.malkusch.ha.automation.application.dehumidifier;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.automation.model.Watt;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.automation.model.dehumidifier.TurnOffDehumidifiersRule;
import de.malkusch.ha.automation.model.dehumidifier.TurnOnDehumidifiersRule;
import de.malkusch.ha.automation.model.heater.Heater;
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
    }

    ScheduleDehumidiferRulesApplicationService(DehumidifierRepository dehumidifiers, RuleScheduler scheduler,
            Electricity electricity, Heater heater, DehumidifierProperties properties) {

        var buffer = new Watt(properties.buffer);

        var turnOn = new TurnOnDehumidifiersRule(dehumidifiers, electricity, heater, buffer, properties.window,
                properties.evaluationRate);
        scheduler.schedule(turnOn);

        var turnOff = new TurnOffDehumidifiersRule(dehumidifiers, electricity, heater, buffer, properties.window,
                properties.evaluationRate);
        scheduler.schedule(turnOff);
    }
}
