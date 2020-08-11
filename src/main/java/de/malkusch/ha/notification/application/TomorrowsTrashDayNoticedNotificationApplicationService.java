package de.malkusch.ha.notification.application;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.trashday.CheckTrashDayService.TomorrowsTrashDayNoticed;
import de.malkusch.ha.notification.model.Notification;
import de.malkusch.ha.notification.model.NotificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class TomorrowsTrashDayNoticedNotificationApplicationService {

    private final NotificationService notificationService;

    @EventListener
    public void onTrashDay(TomorrowsTrashDayNoticed event) {
        var message = String.format("Morgen kommt die Müllabfuhr: %s", event);
        var notification = new Notification(message);
        notificationService.send(notification);
    }

}
