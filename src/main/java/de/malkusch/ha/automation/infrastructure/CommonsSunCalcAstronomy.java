package de.malkusch.ha.automation.infrastructure;

import static java.util.Arrays.asList;
import static org.shredzone.commons.suncalc.SunTimes.Twilight.ASTRONOMICAL;
import static org.shredzone.commons.suncalc.SunTimes.Twilight.CIVIL;
import static org.shredzone.commons.suncalc.SunTimes.Twilight.NAUTICAL;

import java.time.LocalDate;
import java.util.List;

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

@Service
class CommonsSunCalcAstronomy implements Astronomy {

    private final double latitude;
    private final double longitude;

    CommonsSunCalcAstronomy(LocationProperties locationProperties) {
        this.latitude = Double.valueOf(locationProperties.latitude);
        this.longitude = Double.valueOf(locationProperties.longitude);
    }

    @Override
    public List<AstronomicalEvent> calculateEvents(LocalDate date) {
        var astronomical = calculate(ASTRONOMICAL, date);
        var nautical = calculate(NAUTICAL, date);
        var civil = calculate(CIVIL, date);

        return asList(new AstronomicalSunriseStarted(astronomical.getRise().toLocalTime()),
                new AstronomicalSunsetStarted(astronomical.getSet().toLocalTime()),
                new NauticalSunriseStarted(nautical.getRise().toLocalTime()),
                new NauticalSunsetStarted(nautical.getSet().toLocalTime()),
                new CivilSunriseStarted(civil.getRise().toLocalTime()),
                new CivilSunsetStarted(civil.getSet().toLocalTime()));
    }

    private SunTimes calculate(Twilight twilight, LocalDate date) {
        return SunTimes.compute().on(date).latitude(latitude).longitude(longitude).twilight(twilight).execute();
    }
}
