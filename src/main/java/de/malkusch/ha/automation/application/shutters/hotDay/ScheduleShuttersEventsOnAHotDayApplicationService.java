package de.malkusch.ha.automation.application.shutters.hotDay;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.astronomy.Astronomy;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.shared.infrastructure.event.EventScheduler;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ScheduleShuttersEventsOnAHotDayApplicationService {

    private final Astronomy astronomy;
    private final ShutterRepository shutters;
    private final EventScheduler eventScheduler;

    ScheduleShuttersEventsOnAHotDayApplicationService(Astronomy astronomy, ShutterRepository shutters,
            EventScheduler eventScheduler) {
        this.astronomy = astronomy;
        this.shutters = shutters;
        this.eventScheduler = eventScheduler;

        scheduleSunLightStarted();
    }

    @Scheduled(cron = "59 59 03 * * *")
    public void scheduleSunLightStarted() {

        eventScheduler.cancel(DirectSunLightStarted.class);
        eventScheduler.cancel(DirectSunLightEnded.class);

        for (var shutter : shutters.findAll()) {
            var start = astronomy.timeOfAzimuth(shutter.directSunLightRange.start());
            var event = new DirectSunLightStarted(start, shutter.id);
            log.info("Scheduling DirectSunLightStarted at {} for {}", start, shutter);
            eventScheduler.publishAt(event, event.time());
        }
    }

    @EventListener
    public void scheduleSunLightEnded(ShutterClosedOnAHotDay closed) {
        var shutter = shutters.find(closed.shutter());
        var end = astronomy.timeOfAzimuth(shutter.directSunLightRange.end());
        var event = new DirectSunLightEnded(end, shutter.id);
        log.info("Scheduling DirectSunLightEnded at {} for {}", end, shutter);
        eventScheduler.publishAt(event, event.time());
    }
}
