package de.malkusch.ha.automation.application.shutters;

import java.time.Duration;

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

    ScheduleWindProtectionApplicationService(RuleScheduler scheduler,
            @Value("${shutters.wind-protection.evaluation-rate}") Duration evaluationRate,
            WindProtectionService<Shutter> shutterWindProtectionService,
            WindProtectionService<Blind> blindWindProtectionService, ShutterRepository shutters, Weather weather) {

        var rule = new WindProtectionRule(evaluationRate, shutters, shutterWindProtectionService,
                blindWindProtectionService, weather);
        scheduler.schedule(rule);
    }
}
