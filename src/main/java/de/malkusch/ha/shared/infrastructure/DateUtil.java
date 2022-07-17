package de.malkusch.ha.shared.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
}
