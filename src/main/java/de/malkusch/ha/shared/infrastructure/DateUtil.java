package de.malkusch.ha.shared.infrastructure;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtil {

    public static long toTimestamp(LocalDate date) {
        return toTimestamp(date.atStartOfDay());
    }

    public static long toTimestamp(LocalDateTime date) {
        return date.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
