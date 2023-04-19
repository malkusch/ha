package de.malkusch.ha.shared.infrastructure;

import static java.time.ZoneId.systemDefault;
import static java.util.Locale.ENGLISH;
import static net.time4j.format.TextWidth.NARROW;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import lombok.extern.slf4j.Slf4j;
import net.time4j.PrettyTime;

@Slf4j
public class DateUtil {

    public static long toTimestamp(LocalDate date) {
        return toTimestamp(date.atStartOfDay());
    }

    public static long toTimestamp(LocalDateTime date) {
        return toTimestamp(date, ZoneId.systemDefault());
    }

    public static long toTimestamp(LocalDateTime date, ZoneId zone) {
        return date.atZone(zone).toEpochSecond();
    }

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter //
            .ofPattern("HH:mm") //
            .withZone(systemDefault());

    public static String formatTime(Instant time) {
        return TIME_FORMATTER.format(time);
    }

    public static String formatTime(LocalTime time) {
        return TIME_FORMATTER.format(time);
    }

    private static final PrettyTime DURATION_FORMATTER = PrettyTime.of(ENGLISH);

    public static String formatDuration(Duration duration) {
        try {
            return DURATION_FORMATTER.print(duration, NARROW);

        } catch (Exception e) {
            log.error("Error formating {}", duration, e);
            return duration.toString();
        }
    }

    public static String formatSeconds(long seconds) {
        return formatDuration(Duration.ofSeconds(seconds));
    }
}
