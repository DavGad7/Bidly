package com.bidly.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private DateUtil() {}

    public static String formatDateTime(LocalDateTime dt) {
        if (dt == null) {
            return "";
        }
        return dt.format(DATETIME_FMT);
    }

    // "3d 4h", "2h 15m" or "45m"
    public static String formatTimeRemaining(LocalDateTime endTime) {
        if (endTime == null) {
            return "";
        }
        Duration d = Duration.between(LocalDateTime.now(), endTime);
        if (d.isNegative() || d.isZero()) {
            return "Ended";
        }
        long days = d.toDays();
        long hours = d.toHoursPart();
        long minutes = d.toMinutesPart();

        if (days > 0) {
            return days + "d " + hours + "h";
        } else if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }
}
