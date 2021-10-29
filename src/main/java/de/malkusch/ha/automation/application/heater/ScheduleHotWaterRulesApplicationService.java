package de.malkusch.ha.automation.application.heater;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.automation.model.heater.ResetHotWaterOperationModeRule;
import lombok.Data;

@Service
public final class ScheduleHotWaterRulesApplicationService {

    @ConfigurationProperties("buderus.hot-water.rules.reset-program")
    @Component
    @Data
    public static class HotWaterRulesProperties {
        private Duration evaluationRate;
    }

    ScheduleHotWaterRulesApplicationService(Heater heater, RuleScheduler scheduler,
            HotWaterRulesProperties properties) {

        var reset = new ResetHotWaterOperationModeRule(heater, properties.evaluationRate);
        scheduler.schedule(reset);
    }
}
