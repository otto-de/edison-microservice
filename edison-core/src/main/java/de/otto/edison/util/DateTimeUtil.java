package de.otto.edison.util;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * Utility for parsing date-time strings that may use different offset formats,
 * e.g. "+02:00" (ISO-8601) or "+0200" (as produced by git-commit-id-plugin).
 */
public final class DateTimeUtil {

    /**
     * Accepts ISO local date-time with any of these offset variants:
     * <ul>
     *   <li>{@code Z}</li>
     *   <li>{@code +HH:MM} / {@code +HH:MM:ss}  (ISO-8601)</li>
     *   <li>{@code +HHMM} / {@code +HHMMss}      (git-commit-id-plugin)</li>
     * </ul>
     */
    private static final DateTimeFormatter LENIENT_FORMATTER = new DateTimeFormatterBuilder()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .optionalStart().appendOffset("+HHMMss", "Z").optionalEnd()
            .optionalStart().appendOffset("+HH:MM:ss", "Z").optionalEnd()
            .optionalStart().appendOffset("+HH:MM", "Z").optionalEnd()
            .optionalStart().appendOffset("+HHMM", "Z").optionalEnd()
            .toFormatter();

    private DateTimeUtil() {}

    /**
     * Parses a date-time string to {@link OffsetDateTime}, tolerating both
     * {@code +0200} and {@code +02:00} offset styles. Returns {@code null}
     * for {@code null} or empty input instead of throwing.
     */
    public static OffsetDateTime parse(final String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        return OffsetDateTime.parse(dateTime, LENIENT_FORMATTER);
    }
}
