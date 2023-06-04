package de.malkusch.ha.automation.application.shutters;

import java.time.Duration;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.shutters.Blind;
import de.malkusch.ha.automation.model.shutters.Shutter;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.automation.model.shutters.WindProtectionRule;
import de.malkusch.ha.automation.model.shutters.WindProtectionService;
import de.malkusch.ha.automation.model.weather.Weather;

@Service
public class ScheduleWindProtectionApplicationService {

    private final Weather weather;
    private final WindProtectionService<Blind> blindWindProtectionService;
    private final WindProtectionService<Shutter> shutterWindProtectionService;

    ScheduleWindProtectionApplicationService(RuleScheduler scheduler,
            @Value("${shutters.wind-protection.evaluation-rate}") Duration evaluationRate,
            WindProtectionService<Shutter> shutterWindProtectionService,
            WindProtectionService<Blind> blindWindProtectionService, ShutterRepository shutters, Weather weather) {

        this.weather = weather;
        this.blindWindProtectionService = blindWindProtectionService;
        this.shutterWindProtectionService = shutterWindProtectionService;

        var rule = new WindProtectionRule(evaluationRate, shutters, shutterWindProtectionService,
                blindWindProtectionService, weather);
        scheduler.schedule(rule);
    }

    public WindProtectionState windProtectionState() {
        var windSpeed = query(weather::windspeed);
        var blindThreshold = blindWindProtectionService.protectThreshold.toString();
        var shutterThreshold = shutterWindProtectionService.protectThreshold.toString();

        return new WindProtectionState(windSpeed, blindThreshold, shutterThreshold);
    }

    public static record WindProtectionState(String windSpeed, String blindThreshold, String shutterThreshold) {
    }

    private static String query(Callable<Object> query) {
        try {
            return query.call().toString();
        } catch (Exception e) {
            return String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
