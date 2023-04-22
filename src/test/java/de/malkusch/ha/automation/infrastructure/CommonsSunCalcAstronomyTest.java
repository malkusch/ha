package de.malkusch.ha.automation.infrastructure;

import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.AstronomicalSunriseStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.AstronomicalSunsetStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.CivilSunriseStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.CivilSunsetStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.NauticalSunriseStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.NauticalSunsetStarted;
import de.malkusch.ha.automation.model.astronomy.Azimuth;
import de.malkusch.ha.automation.model.geo.Location;

public class CommonsSunCalcAstronomyTest {

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");
    private static final CommonsSunCalcAstronomy ASTRONOMY = new CommonsSunCalcAstronomy(berlin());

    private final static Map<LocalDate, CalculatedEvents> ONE_YEAR_EVENTS = Arrays.stream(oneYear()) //
            .map(it -> calculatedEvents(it, ASTRONOMY.calculateEvents(it.atStartOfDay(ZONE)))) //
            .collect(toMap(CalculatedEvents::date, it -> it));

    static Collection<CalculatedEvents> ONE_YEAR_EVENTS() {
        return ONE_YEAR_EVENTS.values();
    }

    private record CalculatedEvents(LocalDate date, AstronomicalSunriseStarted astronomicalSunriseStarted,
            NauticalSunriseStarted nauticalSunriseStarted, CivilSunriseStarted civilSunriseStarted,
            CivilSunsetStarted civilSunsetStarted, NauticalSunsetStarted nauticalSunsetStarted,
            AstronomicalSunsetStarted astronomicalSunsetStarted) {
    }

    private static Location berlin() {
        return new Location(52.518044, 13.408246);
    }

    private static LocalDate[] oneYear() {
        var days = new ArrayList<LocalDate>();
        for (var i = LocalDate.parse("2021-01-01"); i.isBefore(LocalDate.parse("2022-01-01")); i = i.plusDays(1)) {
            days.add(i);
        }
        return days.toArray(LocalDate[]::new);
    }

    @ParameterizedTest
    @MethodSource("ONE_YEAR_EVENTS")
    public void calculatedEventsShouldBeOrdered(CalculatedEvents events) {
        var astronomicalSunrise = events.astronomicalSunriseStarted.time();
        var nauticalSunrise = events.nauticalSunriseStarted.time();
        var civilSunrise = events.civilSunriseStarted.time();
        var civilSunset = events.civilSunsetStarted.time();
        var nauticalSunset = events.nauticalSunsetStarted.time();
        var astronomicalSunset = events.astronomicalSunsetStarted.time();

        assertOrderedSunTimes(astronomicalSunrise, nauticalSunrise, civilSunrise, civilSunset, nauticalSunset,
                astronomicalSunset);
    }

    @ParameterizedTest
    @MethodSource("ONE_YEAR_EVENTS")
    public void calculatedEventsShouldStartWithinExpectedRange(CalculatedEvents events) {
        var astronomicalSunrise = events.astronomicalSunriseStarted.time();
        var nauticalSunrise = events.nauticalSunriseStarted.time();
        var civilSunrise = events.civilSunriseStarted.time();
        var civilSunset = events.civilSunsetStarted.time();
        var nauticalSunset = events.nauticalSunsetStarted.time();
        var astronomicalSunset = events.astronomicalSunsetStarted.time();

        assertBetween("01:00", "07:00", astronomicalSunrise);
        assertBetween("02:00", "07:30", nauticalSunrise);
        assertBetween("03:00", "08:00", civilSunrise);
        assertBetween("16:00", "23:00", civilSunset);
        assertBetween("18:00", "00:00", nauticalSunset);
        assertBetween("19:00", "01:00", astronomicalSunset);
    }

    @ParameterizedTest
    @MethodSource("ONE_YEAR_EVENTS")
    public void calculatedEventsShouldNotChangeMuch(CalculatedEvents events) {
        var nextDay = events.date.plusDays(1);
        if (nextDay.getYear() != events.date.getYear()) {
            nextDay = events.date.withDayOfYear(1);
        }
        var next = ONE_YEAR_EVENTS.get(nextDay);

        assertAlmostEquals(events, next);
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            2021-01-01, 07:36:01, 16:44:29
            2021-05-03, 04:51:13, 21:16:53
            2021-06-21, 03:53:01, 22:23:39
            2021-09-01, 05:41:40, 20:29:54
            2021-12-21, 07:33:25, 16:35:52
            2021-12-31, 07:36:03, 16:43:15
            """)
    public void testCalculatedTimes(String dateString, String expectedCivilRise, String expectedCivilSunset) {
        var date = LocalDate.parse(dateString);

        var events = asMap(ASTRONOMY.calculateEvents(date.atStartOfDay(ZONE)));

        var civilSunrise = events.get(CivilSunriseStarted.class);
        assertAlmostEquals(expectedCivilRise, civilSunrise);
        var civilSunset = events.get(CivilSunsetStarted.class);
        assertAlmostEquals(expectedCivilSunset, civilSunset);
    }

    private static void assertAlmostEquals(CalculatedEvents events, CalculatedEvents others) {
        assertAlmostEquals(events, others, CalculatedEvents::astronomicalSunriseStarted);
        assertAlmostEquals(events, others, CalculatedEvents::nauticalSunriseStarted);
        assertAlmostEquals(events, others, CalculatedEvents::civilSunriseStarted);
        assertAlmostEquals(events, others, CalculatedEvents::civilSunsetStarted);
        assertAlmostEquals(events, others, CalculatedEvents::nauticalSunsetStarted);
        assertAlmostEquals(events, others, CalculatedEvents::astronomicalSunsetStarted);
    }

    private static <T extends AstronomicalEvent> void assertAlmostEquals(CalculatedEvents events,
            CalculatedEvents others, Function<CalculatedEvents, T> event) {

        var a = event.apply(events);
        var b = event.apply(others);
        assertEquals(a.getClass(), b.getClass());

        assertTimeAlmostEquals(a.dateTime(), b.dateTime());
    }

    private static void assertAlmostEquals(String expected, LocalTime time) {
        assertAlmostEquals(LocalTime.parse(expected), time);
    }

    private static void assertTimeAlmostEquals(ZonedDateTime expected, ZonedDateTime other) {
        var otherTime = other.toOffsetDateTime().toOffsetTime();
        var expectedTime = expected.toOffsetDateTime().toOffsetTime();
        assertAlmostEquals(expectedTime, otherTime);
    }

    private static final Duration ALMOST_EQUALS = Duration.ofMinutes(20);

    private static void assertAlmostEquals(Temporal expected, Temporal time) {
        var duration = Duration.between(expected, time).abs();
        if (duration.toHours() >= 23) {
            duration = duration.minusDays(1).abs();
        }

        assertTrue(duration.compareTo(ALMOST_EQUALS) <= 0, String.format("%s is not expected %s", time, expected));
    }

    private static Map<Class<?>, LocalTime> asMap(List<AstronomicalEvent> events) {
        return events.stream() //
                .collect(toMap(Object::getClass, AstronomicalEvent::time));
    }

    private static CalculatedEvents calculatedEvents(LocalDate date, List<AstronomicalEvent> events) {
        var map = events.stream() //
                .collect(toMap(Object::getClass, it -> it));
        var astronomicalSunrise = (AstronomicalSunriseStarted) map.get(AstronomicalSunriseStarted.class);
        var nauticalSunrise = (NauticalSunriseStarted) map.get(NauticalSunriseStarted.class);
        var civilSunrise = (CivilSunriseStarted) map.get(CivilSunriseStarted.class);
        var civilSunset = (CivilSunsetStarted) map.get(CivilSunsetStarted.class);
        var nauticalSunset = (NauticalSunsetStarted) map.get(NauticalSunsetStarted.class);
        var astronomicalSunset = (AstronomicalSunsetStarted) map.get(AstronomicalSunsetStarted.class);

        return new CalculatedEvents(date, astronomicalSunrise, nauticalSunrise, civilSunrise, civilSunset,
                nauticalSunset, astronomicalSunset);
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
        var time = ASTRONOMY.timeOfAzimuth(azimuth, date.atStartOfDay(ZoneId.of("Europe/Berlin")));

        assertBetween("03:30", "22:00", time);
    }

    private static void assertBetween(String startTime, String endTime, LocalTime time) {
        var start = LocalTime.parse(startTime);
        var end = LocalTime.parse(endTime);
        assertBetween(start, end, time);
    }

    private static void assertBetween(LocalTime start, LocalTime end, LocalTime time) {
        if (start.isBefore(end)) {
            assertTrue(_isBetweenWithinOneDay(start, end, time), String.format("%s < %s < %s", start, time, end));

        } else {
            assertTrue(_isBetweenWithinOneDay(start, LocalTime.MAX, time) //
                    || _isBetweenWithinOneDay(LocalTime.MIN, start, time), //
                    String.format("%s < %s < %s", start, time, end));

        }
    }

    private static void assertOrderedSunTimes(LocalTime... times) {
        Function<Integer, LocalTime> getTime = (i) -> {
            var index = Math.floorMod(i, times.length);
            return times[index];
        };

        for (int i = 0; i < times.length; i++) {
            var start = getTime.apply(i - 1);
            var time = getTime.apply(i);
            var end = getTime.apply(i + 1);

            assertBetween(start, end, time);
        }
    }

    private static boolean _isBetweenWithinOneDay(LocalTime start, LocalTime end, LocalTime time) {
        assert (start.isBefore(end));
        return time.isAfter(start) && time.isBefore(end);
    }
}
