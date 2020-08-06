package de.malkusch.ha.automation.application.dehumidifier;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.automation.model.Watt;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.automation.model.dehumidifier.TurnOffDehumidifierRule;
import de.malkusch.ha.automation.model.dehumidifier.TurnOnDehumidifierRule;
import lombok.Data;

@Service
public final class SchedulDehumidiferRulesApplicationService {

    @ConfigurationProperties("dehumidifier.rules")
    @Component
    @Data
    public static class DehumidifierProperties {
        private int buffer;
        private Duration window;
        private Duration evaluationRate;
    }

    SchedulDehumidiferRulesApplicationService(DehumidifierRepository dehumidifiers, RuleScheduler scheduler,
            Electricity electricity, DehumidifierProperties properties) {

        var buffer = new Watt(properties.buffer);

        for (var dehumidifier : dehumidifiers.findAll()) {
            var turnOn = new TurnOnDehumidifierRule(dehumidifier, electricity, buffer, properties.window,
                    properties.evaluationRate);
            scheduler.schedule(turnOn);

            var turnOff = new TurnOffDehumidifierRule(dehumidifier, electricity, buffer, properties.window,
                    properties.evaluationRate);
            scheduler.schedule(turnOff);
        }
    }
}
