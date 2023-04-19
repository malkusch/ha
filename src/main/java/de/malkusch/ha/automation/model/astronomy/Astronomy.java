package de.malkusch.ha.automation.model.astronomy;

import static de.malkusch.ha.shared.infrastructure.DateUtil.defaultZone;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

public interface Astronomy {

    default List<AstronomicalEvent> calculateEvents(LocalDate date) {
        return calculateEvents(date.atStartOfDay(defaultZone()));
    }

    List<AstronomicalEvent> calculateEvents(ZonedDateTime date);

    default LocalTime timeOfAzimuth(Azimuth azimuth) {
        return timeOfAzimuth(azimuth, ZonedDateTime.now());
    }

    LocalTime timeOfAzimuth(Azimuth azimuth, ZonedDateTime day);

}
