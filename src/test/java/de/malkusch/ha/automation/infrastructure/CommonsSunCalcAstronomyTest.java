package de.malkusch.ha.automation.infrastructure;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.malkusch.ha.automation.model.astronomy.DawnStarted;
import de.malkusch.ha.automation.model.astronomy.DuskStarted;
import de.malkusch.ha.automation.model.astronomy.NightStarted;
import de.malkusch.ha.shared.infrastructure.event.EventScheduler;

@ExtendWith(MockitoExtension.class)
public class CommonsSunCalcAstronomyTest {

    @Mock
    private EventScheduler eventScheduler;

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    private static List<Clock> oneYear() {
        var days = new ArrayList<Clock>();
        for (var i = LocalDate.parse("2021-01-01"); i.isBefore(LocalDate.parse("2022-01-01")); i = i.plusDays(1)) {
            var instant = i.atTime(2, 59, 59).atZone(ZONE).toInstant();
            days.add(Clock.fixed(instant, ZONE));
        }
        return days;
    }

    @ParameterizedTest
    @MethodSource("oneYear")
    public void shouldPublishEvents(Clock clock) {
        var location = new LocationProperties();
        location.latitude = "52.518044";
        location.longitude = "13.408246";
        var astronomy = new CommonsSunCalcAstronomy(location, clock, eventScheduler);

        astronomy.calculateTimesAndScheduleEvents();

        verify(eventScheduler, atLeastOnce()).publishAt(any(DawnStarted.class), any());
        verify(eventScheduler, atLeastOnce()).publishAt(any(DuskStarted.class), any());
        verify(eventScheduler, atLeastOnce()).publishAt(any(NightStarted.class), any());
    }
}
