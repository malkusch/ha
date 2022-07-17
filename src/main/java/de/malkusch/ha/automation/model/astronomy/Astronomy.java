package de.malkusch.ha.automation.model.astronomy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

public interface Astronomy {

    List<AstronomicalEvent> calculateEvents(LocalDate date);

    default LocalTime timeOfAzimuth(Azimuth azimuth) {
        return timeOfAzimuth(azimuth, ZonedDateTime.now());
    }
    
    LocalTime timeOfAzimuth(Azimuth azimuth, ZonedDateTime day);

}
