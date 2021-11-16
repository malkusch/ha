package de.malkusch.ha.automation.application.astrononmy;

import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.astronomy.Astronomy;
import de.malkusch.ha.shared.infrastructure.event.EventScheduler;

@Service
public class ScheduleAstronomicalEventsApplicationService {

    private final Astronomy astronomy;
    private final EventScheduler eventScheduler;

    ScheduleAstronomicalEventsApplicationService(Astronomy astronomy, EventScheduler eventScheduler) {
        this.astronomy = astronomy;
        this.eventScheduler = eventScheduler;

        scheduleEvents();
    }

    @Scheduled(cron = "59 59 02 * * *")
    public void scheduleEvents() {
        var events = astronomy.calculateEvents(LocalDate.now());
        for (var event : events) {
            eventScheduler.cancel(event.getClass());
            eventScheduler.publishAt(event, event.time());
        }
    }
}
