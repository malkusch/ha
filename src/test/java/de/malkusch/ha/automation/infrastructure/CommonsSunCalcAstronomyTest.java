package de.malkusch.ha.automation.infrastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.malkusch.ha.automation.model.astronomy.Azimuth;

public class CommonsSunCalcAstronomyTest {

    private static List<LocalDate> oneYear() {
        var days = new ArrayList<LocalDate>();
        for (var i = LocalDate.parse("2021-01-01"); i.isBefore(LocalDate.parse("2022-01-01")); i = i.plusDays(1)) {
            days.add(i);
        }
        return days;
    }

    @ParameterizedTest
    @MethodSource("oneYear")
    public void shouldCalculateEvents(LocalDate date) {
        var location = new LocationProperties();
        location.latitude = "52.518044";
        location.longitude = "13.408246";
        var astronomy = new CommonsSunCalcAstronomy(location);

        astronomy.calculateEvents(date);
    }

    private static List<Arguments> timeOfAzimuthCases() {
        var azimuths = new ArrayList<Arguments>();
        for (var date : oneYear()) {
            azimuths.add(Arguments.of(date, new Azimuth(78)));
            azimuths.add(Arguments.of(date, new Azimuth(133.33)));
            azimuths.add(Arguments.of(date, new Azimuth(290)));
        }

        return azimuths;
    }

    @ParameterizedTest
    @MethodSource("timeOfAzimuthCases")
    public void timeOfAzimuthShouldFindTime(LocalDate date, Azimuth azimuth) {
        var location = new LocationProperties();
        location.latitude = "52.518044";
        location.longitude = "13.408246";
        var astronomy = new CommonsSunCalcAstronomy(location);

        var time = astronomy.timeOfAzimuth(azimuth, date.atStartOfDay(ZoneId.of("Europe/Berlin")));

        assertTrue(time.isAfter(LocalTime.parse("03:30:00")), time.toString());
        assertTrue(time.isBefore(LocalTime.parse("22:00:00")), time.toString());
    }
}
