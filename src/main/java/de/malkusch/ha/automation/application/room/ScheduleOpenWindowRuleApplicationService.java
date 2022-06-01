package de.malkusch.ha.automation.application.room;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler;
import de.malkusch.ha.automation.model.room.OpenWindowRule;

@Service
public final class ScheduleOpenWindowRuleApplicationService {

    ScheduleOpenWindowRuleApplicationService(RuleScheduler scheduler, OpenWindowRule rule) {
        scheduler.schedule(rule);
    }
}
