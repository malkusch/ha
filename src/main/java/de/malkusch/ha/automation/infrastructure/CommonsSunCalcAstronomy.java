package de.malkusch.ha.automation.infrastructure;

import static org.shredzone.commons.suncalc.SunTimes.Twilight.ASTRONOMICAL;
import static org.shredzone.commons.suncalc.SunTimes.Twilight.CIVIL;

import java.time.Clock;
import java.time.LocalTime;

import org.shredzone.commons.suncalc.SunTimes;
import org.shredzone.commons.suncalc.SunTimes.Twilight;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.astronomy.Astronomy;
import de.malkusch.ha.automation.model.astronomy.DawnStarted;
import de.malkusch.ha.automation.model.astronomy.DuskStarted;
import de.malkusch.ha.automation.model.astronomy.NightStarted;
import de.malkusch.ha.shared.infrastructure.event.Event;
import de.malkusch.ha.shared.infrastructure.event.EventScheduler;

@Service
class CommonsSunCalcAstronomy implements Astronomy {

    private final EventScheduler eventScheduler;
    private final Clock clock;
    private final double latitude;
    private final double longitude;

    CommonsSunCalcAstronomy(LocationProperties locationProperties, Clock clock, EventScheduler eventScheduler) {
        this.clock = clock;
        this.eventScheduler = eventScheduler;
        this.latitude = Double.valueOf(locationProperties.latitude);
        this.longitude = Double.valueOf(locationProperties.longitude);

        calculateTimesAndScheduleEvents();
    }

    private volatile LocalTime dawn;
    private volatile LocalTime night;

    @Override
    public boolean isNight() {
        var now = LocalTime.now();
        return now.isAfter(night) && now.isBefore(dawn);
    }

    @Scheduled(cron = "59 59 02 * * *")
    void calculateTimesAndScheduleEvents() {
        dawn = calculate(CIVIL).getRise().toLocalTime();
        var dusk = calculate(CIVIL).getSet().toLocalTime();
        night = calculate(ASTRONOMICAL).getSet().toLocalTime();

        scheduleEvent(dawn, new DawnStarted());
        scheduleEvent(dusk, new DuskStarted());
        scheduleEvent(night, new NightStarted());
    }

    private SunTimes calculate(Twilight twilight) {
        return SunTimes.compute().on(clock.instant()).latitude(latitude).longitude(longitude).twilight(twilight)
                .execute();
    }

    private void scheduleEvent(LocalTime time, Event event) {
        eventScheduler.cancel(event.getClass());
        eventScheduler.publishAt(event, time);
    }
}
