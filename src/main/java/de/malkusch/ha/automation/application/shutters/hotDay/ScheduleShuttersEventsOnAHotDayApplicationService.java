package de.malkusch.ha.automation.application.shutters.hotDay;

import de.malkusch.ha.automation.model.astronomy.Astronomy;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.shared.infrastructure.DateUtil;
import de.malkusch.ha.shared.infrastructure.event.EventScheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;

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
            if (shutter.directSunLightRange.isEmpty()) {
                log.debug("Skipping shutter {} with no direct sun light", shutter);
                continue;
            }
            var start = astronomy.timeOfAzimuth(shutter.directSunLightRange.start());
            var event = new DirectSunLightStarted(start, shutter.id);
            log.debug("Scheduling sun light start for shutter {} at {}", shutter, formatTime(start));
            eventScheduler.publishAt(event, event.time());
        }
    }

    @EventListener
    public void scheduleSunLightEnded(ShutterClosedOnAHotDay closed) {
        var shutter = shutters.find(closed.shutter());
        var end = astronomy.timeOfAzimuth(shutter.directSunLightRange.end());
        var event = new DirectSunLightEnded(end, shutter.id);
        eventScheduler.publishAt(event, event.time());
    }
}
