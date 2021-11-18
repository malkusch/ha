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
import de.malkusch.ha.automation.model.weather.Cloudiness;
import de.malkusch.ha.automation.model.weather.Weather;
import lombok.Data;

@Service
public final class ScheduleAvoidGridHeaterRuleApplicationService {

    @ConfigurationProperties("buderus.heater.avoid-grid-rule")
    @Component
    @Data
    public static class Properties {
        private double minCapacity;
        private double maxCloudiness;
        private int excessThreshold;
        private Duration evaluationRate;
    }

    ScheduleAvoidGridHeaterRuleApplicationService(Heater heater, Electricity electricity, Weather weather,
            TemporaryDayTemperatureService temperatureService, RuleScheduler scheduler, Properties properties) {

        var capacity = new Capacity(properties.minCapacity);
        var maxCloudiness = new Cloudiness(properties.maxCloudiness);
        var excessThreshold = new Watt(properties.excessThreshold);

        var rule = new AvoidGridHeaterRule(capacity, maxCloudiness, excessThreshold, properties.evaluationRate,
                electricity, heater, weather, temperatureService);
        // scheduler.schedule(rule);
    }
}
