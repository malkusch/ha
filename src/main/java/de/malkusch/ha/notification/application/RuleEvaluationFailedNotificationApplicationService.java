package de.malkusch.ha.notification.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.rule.RuleScheduler.RuleEvaluationFailed;
import de.malkusch.ha.notification.model.Notification;
import de.malkusch.ha.notification.model.NotificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class RuleEvaluationFailedNotificationApplicationService {

    private final NotificationService notificationService;

    @EventListener
    public void onRuleEvaluationFailed(RuleEvaluationFailed event) {
        var message = String.format("%s failed [%s]: %s", event.rule, event.reference, event.cause);
        var notification = new Notification(message);
        notificationService.send(notification);
    }
}