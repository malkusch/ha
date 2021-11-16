package de.malkusch.ha.automation.infrastructure;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
}
