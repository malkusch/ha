package de.malkusch.ha.automation.infrastructure;

import static java.util.Arrays.asList;
import static org.shredzone.commons.suncalc.SunTimes.Twilight.ASTRONOMICAL;
import static org.shredzone.commons.suncalc.SunTimes.Twilight.CIVIL;
import static org.shredzone.commons.suncalc.SunTimes.Twilight.NAUTICAL;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.shredzone.commons.suncalc.SunPosition;
import org.shredzone.commons.suncalc.SunTimes;
import org.shredzone.commons.suncalc.SunTimes.Twilight;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.AstronomicalSunriseStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.AstronomicalSunsetStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.CivilSunriseStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.CivilSunsetStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.NauticalSunriseStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.NauticalSunsetStarted;
import de.malkusch.ha.automation.model.astronomy.Astronomy;
import de.malkusch.ha.automation.model.astronomy.Azimuth;
import de.malkusch.ha.automation.model.geo.Location;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class CommonsSunCalcAstronomy implements Astronomy {

    private final Location location;

    @Override
    public List<AstronomicalEvent> calculateEvents(ZonedDateTime date) {
        var astronomical = calculate(ASTRONOMICAL, date);
        var nautical = calculate(NAUTICAL, date);
        var civil = calculate(CIVIL, date);

        return asList(new AstronomicalSunriseStarted(astronomical.getRise()),
                new AstronomicalSunsetStarted(astronomical.getSet()), new NauticalSunriseStarted(nautical.getRise()),
                new NauticalSunsetStarted(nautical.getSet()), new CivilSunriseStarted(civil.getRise()),
                new CivilSunsetStarted(civil.getSet()));
    }

    private SunTimes calculate(Twilight twilight, ZonedDateTime date) {
        return SunTimes.compute() //
                .on(date) //
                .latitude(location.latitude()) //
                .longitude(location.longitude()) //
                .twilight(twilight) //
                .execute();
    }

    @Override
    public LocalTime timeOfAzimuth(Azimuth azimuth, ZonedDateTime day) {
        var start = day.truncatedTo(ChronoUnit.DAYS);
        var end = start.plusDays(1);

        var passedZero = false;
        for (var time = start; time.isBefore(end); time = time.plusMinutes(15)) {
            var position = SunPosition.compute() //
                    .on(time) //
                    .latitude(location.latitude()) //
                    .longitude(location.longitude()) //
                    .execute();
            if (!passedZero) {
                if (position.getAzimuth() <= azimuth.angle()) {
                    passedZero = true;
                }
                continue;
            }
            if (position.getAzimuth() >= azimuth.angle()) {
                return time.toLocalTime();
            }
        }
        throw new IllegalStateException("Couldn't find time when azimuth is " + azimuth);
    }
}
