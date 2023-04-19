package de.malkusch.ha.shared.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DateUtilTest {

    final static String FORMAT_DURATION_CASES_SECONDS = """
            PT1s, 1s
            PT120s, 2m
            PT121s, 2m 1s
            PT3600s, 1h
            PT3601s, 1h 1s
            PT11h37m32s, 11h 37m 32s
            PT24h, 1d
            PT24h1s, 1d 1s
            PT24h1m, 1d 1m
            PT25h, 1d 1h
            """;

    final static String FORMAT_DURATION_CASES_MILLISECONDS = """
            PT0.5s, 500ms
            PT1.5s, 1s 500ms
            """;

    @ParameterizedTest
    @CsvSource(textBlock = FORMAT_DURATION_CASES_SECONDS + FORMAT_DURATION_CASES_MILLISECONDS)
    void testFormatDuration(String durationString, String expected) {
        var duration = Duration.parse(durationString);

        var formatted = DateUtil.formatDuration(duration);

        assertEquals(expected, formatted);
    }

    @ParameterizedTest
    @CsvSource(textBlock = FORMAT_DURATION_CASES_SECONDS)
    void testFormatSeconds(String durationString, String expected) {
        var duration = Duration.parse(durationString);
        var seconds = duration.toSeconds();

        var formatted = DateUtil.formatSeconds(seconds);

        assertEquals(expected, formatted);
    }

    private static final ZoneId ZONE = ZoneId.of("Europe/Berlin");

    final static String TO_TIMESTAMP_CASES = """
            2022-12-31, 2022-12-31T00:00:00
            2023-01-01, 2023-01-01T00:00:00
            2023-01-31, 2023-01-31T00:00:00

            2023-02-28, 2023-02-28T00:00:00
            2023-03-01, 2023-03-01T00:00:00

            2023-03-25, 2023-03-25T00:00:00
            2023-03-26, 2023-03-26T00:00:00
            2023-03-27, 2023-03-27T00:00:00

            2023-10-28, 2023-10-28T00:00:00
            2023-10-29, 2023-10-29T00:00:00
            2023-10-30, 2023-10-30T00:00:00

            2024-02-28, 2024-02-28T00:00:00
            2024-02-29, 2024-02-29T00:00:00
            2024-03-01, 2024-03-01T00:00:00
            """;

    @ParameterizedTest
    @CsvSource(textBlock = TO_TIMESTAMP_CASES)
    void testToTimestamp(String dateString, String expectedString) {
        var date = LocalDate.parse(dateString);

        var timestamp = DateUtil.toTimestamp(date, ZONE);

        var expected = timestamp(expectedString);
        assertEquals(timestamp, expected);
    }

    private static long timestamp(String localDateTime) {
        return LocalDateTime.parse(localDateTime).atZone(ZONE).toEpochSecond();
    }
}
