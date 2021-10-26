package de.malkusch.ha.automation.application.heater;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.Capacity;
import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.automation.model.Watt;
import de.malkusch.ha.automation.model.heater.AvoidGridHeaterRule;
import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.automation.model.heater.TemporaryDayTemperatureService;
import lombok.Data;

@Service
public final class ScheduleAvoidGridHeaterRuleApplicationService {

    @ConfigurationProperties("buderus.heater.avoid-grid-rule")
    @Component
    @Data
    public static class Properties {
        private double minCapacity;
        private int excessThreshold;
        private Duration evaluationRate;
    }

    ScheduleAvoidGridHeaterRuleApplicationService(Heater heater, Electricity electricity,
            TemporaryDayTemperatureService temperatureService, RuleScheduler scheduler, Properties properties) {

        var capacity = new Capacity(properties.minCapacity);
        var excessThreshold = new Watt(properties.excessThreshold);

        var rule = new AvoidGridHeaterRule(capacity, excessThreshold, properties.evaluationRate, electricity, heater,
                temperatureService);
        scheduler.schedule(rule);
    }
}
