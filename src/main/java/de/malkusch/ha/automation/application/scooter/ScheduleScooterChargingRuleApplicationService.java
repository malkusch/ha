package de.malkusch.ha.automation.application.scooter;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.scooter.charging.ChargingRule;

@Service
public final class ScheduleScooterChargingRuleApplicationService {

    ScheduleScooterChargingRuleApplicationService(RuleScheduler scheduler, ChargingRule rule) {
        scheduler.schedule(rule);
    }
}
