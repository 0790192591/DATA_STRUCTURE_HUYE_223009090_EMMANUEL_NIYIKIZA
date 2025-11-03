package com.financeportal.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Date/time helpers.
 * - Uses LocalDateTime (no timezone) for application-layer timestamps.
 * - Converts to/from java.sql.Timestamp for JDBC.
 *
 * NOTE: If your app needs timezone-aware handling, consider using OffsetDateTime or ZonedDateTime
 * and storing UTC offsets in DB.
 */
public final class DateUtil {

    // Common patterns
    public static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;           // e.g. 2023-07-22T15:30:00
    public static final DateTimeFormatter FRIENDLY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateUtil() {}

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static String formatIso(LocalDateTime dt) {
        Objects.requireNonNull(dt);
        return dt.format(ISO_FORMAT);
    }

    public static String formatFriendly(LocalDateTime dt) {
        Objects.requireNonNull(dt);
        return dt.format(FRIENDLY_FORMAT);
    }

    public static LocalDateTime parseIso(String s) {
        if (s == null) return null;
        return LocalDateTime.parse(s, ISO_FORMAT);
    }

    public static LocalDateTime parseFriendly(String s) {
        if (s == null) return null;
        return LocalDateTime.parse(s, FRIENDLY_FORMAT);
    }

    public static Timestamp toTimestamp(LocalDateTime dt) {
        return dt == null ? null : Timestamp.valueOf(dt);
    }

    public static LocalDateTime fromTimestamp(Timestamp ts) {
        return ts == null ? null : ts.toLocalDateTime();
    }
}
