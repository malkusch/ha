package de.malkusch.ha.shared.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DateUtilTest {

    @ParameterizedTest
    @CsvSource({ "PT1s, 1s", //
            "PT1.5s, 1s 500ms", //
            "PT120s, 2m", //
            "PT121s, 2m 1s", //
            "PT3600s, 1h", //
            "PT3601s, 1h 1s", //
            "PT11h37m32s, 11h 37m 32s", //
            "PT24h, 1d", //
            "PT24h1s, 1d 1s", //
            "PT24h1m, 1d 1m", //
            "PT25h, 1d 1h", //
    })
    void formatDurationShouldPrint(String durationString, String expected) {
        var duration = Duration.parse(durationString);

        var formatted = DateUtil.formatDuration(duration);

        assertEquals(expected, formatted);
    }

}
