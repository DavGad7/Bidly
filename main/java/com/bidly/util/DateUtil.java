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

    // e.g. 3d 4h, 2h 15m, 45m
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

    public static String formatRelativeTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        Duration d = Duration.between(time, LocalDateTime.now());
        if (d.isNegative() || d.toMinutes() < 1) {
            return "Just now";
        }
        if (d.toDays() > 0) {
            return d.toDays() + " days ago";
        }
        if (d.toHours() > 0) {
            return d.toHours() + " hours ago";
        }
        return d.toMinutes() + " minutes ago";
    }
}

