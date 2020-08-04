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

    @ConfigurationProperties("dehumidifier.rules.turn-on")
    @Component
    @Data
    public static class TurnOnProperties {
        private int buffer;
        private Duration window;
        private Duration evaluationRate;
    }

    @ConfigurationProperties("dehumidifier.rules.turn-off")
    @Component
    @Data
    public static class TurnOffProperties {
        private Duration window;
        private Duration evaluationRate;
    }

    SchedulDehumidiferRulesApplicationService(DehumidifierRepository dehumidifiers, RuleScheduler scheduler,
            Electricity electricity, TurnOnProperties turnOnProperties, TurnOffProperties turnOffProperties) {

        var buffer = new Watt(turnOnProperties.buffer);

        for (var dehumidifier : dehumidifiers.findAll()) {
            var turnOn = new TurnOnDehumidifierRule(dehumidifier, electricity, buffer, turnOnProperties.window,
                    turnOnProperties.evaluationRate);
            scheduler.schedule(turnOn);

            var turnOff = new TurnOffDehumidifierRule(dehumidifier, electricity, buffer, turnOffProperties.window,
                    turnOffProperties.evaluationRate);
            scheduler.schedule(turnOff);
        }
    }
}
