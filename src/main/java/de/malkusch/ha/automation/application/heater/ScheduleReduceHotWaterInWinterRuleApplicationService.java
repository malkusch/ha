package de.malkusch.ha.automation.application.heater;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.automation.model.heater.ReduceHotWaterInWinterRule;
import de.malkusch.ha.automation.model.heater.TemporayHotWaterTemperatureService;
import de.malkusch.ha.automation.model.weather.Cloudiness;
import de.malkusch.ha.automation.model.weather.Weather;
import lombok.Data;

@Service
public final class ScheduleReduceHotWaterInWinterRuleApplicationService {

    @ConfigurationProperties("buderus.hot-water.rules.reduce-in-winter")
    @Component
    @Data
    public static class Properties {
        private Duration evaluationRate;
        private double cloudinessThreshold;
        private int delta;
    }

    ScheduleReduceHotWaterInWinterRuleApplicationService(Weather weather, Heater heater,
            TemporayHotWaterTemperatureService hotWaterService, RuleScheduler scheduler, Properties properties) {

        var threshold = new Cloudiness(properties.cloudinessThreshold);
        var delta = new Temperature(properties.delta);

        var reset = new ReduceHotWaterInWinterRule(weather, threshold, heater, hotWaterService, delta,
                properties.evaluationRate);
        // scheduler.schedule(reset);
    }

}
