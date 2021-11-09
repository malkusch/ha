package de.malkusch.ha.automation.infrastructure;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.function.Function;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import de.malkusch.ha.automation.model.astronomy.Astronomy;
import de.malkusch.ha.automation.model.astronomy.DawnStarted;
import de.malkusch.ha.automation.model.astronomy.DuskStarted;
import de.malkusch.ha.automation.model.astronomy.NightStarted;
import de.malkusch.ha.shared.infrastructure.event.Event;
import de.malkusch.ha.shared.infrastructure.event.EventScheduler;

@Service
class SunriseSunsetLibAstronomy implements Astronomy {

    private final SunriseSunsetCalculator calculator;
    private final EventScheduler eventScheduler;

    SunriseSunsetLibAstronomy(LocationProperties locationProperties, EventScheduler eventScheduler) {

        this.eventScheduler = eventScheduler;
        calculator = new SunriseSunsetCalculator(
                new Location(locationProperties.latitude, locationProperties.longitude), TimeZone.getDefault());

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
        dawn = calculateTimesAndScheduleEvents(calculator::getCivilSunriseForDate, new DawnStarted());
        calculateTimesAndScheduleEvents(calculator::getNauticalSunsetForDate, new DuskStarted());
        night = calculateTimesAndScheduleEvents(calculator::getAstronomicalSunsetForDate, new NightStarted());
    }

    private LocalTime calculateTimesAndScheduleEvents(Function<Calendar, String> calculation, Event event) {
        var now = Calendar.getInstance();
        var time = LocalTime.parse(calculation.apply(now));
        eventScheduler.cancel(event.getClass());
        eventScheduler.publishAt(event, time);
        return time;
    }
}
