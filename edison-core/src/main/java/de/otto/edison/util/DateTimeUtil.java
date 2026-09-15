package de.otto.edison.util;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * Utility for parsing date-time strings that may use different offset formats,
 * e.g. "+02:00" (ISO-8601) or "+0200" (as produced by git-commit-id-plugin),
 * or epoch milliseconds (as produced by Spring Boot's {@code GitProperties} coercion).
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
     * Parses a date-time string to {@link OffsetDateTime}. Handles:
     * <ul>
     *   <li>Epoch milliseconds (e.g. {@code "1747827915000"}) — as returned by Spring Boot's
     *       {@code GitProperties} coercion of {@code commit.time}. Interpreted as UTC.</li>
     *   <li>ISO date-time with {@code +0200} or {@code +02:00} offset styles.</li>
     * </ul>
     * Returns {@code null} for {@code null} or empty input instead of throwing.
     */
    public static OffsetDateTime parse(final String dateTime) {
        try {

            if (dateTime == null || dateTime.isEmpty()) {
                return null;
            }

            if (dateTime.chars().allMatch(Character::isDigit)) {
                return OffsetDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(dateTime)), ZoneOffset.UTC);
            }
            return OffsetDateTime.parse(dateTime, LENIENT_FORMATTER);

        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * Formats the elapsed time between the given instant and now as a human-readable duration,
     * for example {@code "3d 4h 12m"}, {@code "5m 3s"} or {@code "42s"}.
     *
     * @param since the start of the duration, may be {@code null}
     * @return the formatted duration, or {@code null} if {@code since} is {@code null}
     */
    public static String formatDurationSince(final Instant since) {
        if (since == null) {
            return null;
        }
        return formatDuration(Duration.between(since, Instant.now()));
    }

    /**
     * Formats a duration as a human-readable string using the two most significant units.
     *
     * @param duration the duration to format, may be {@code null}
     * @return the formatted duration, or {@code null} if {@code duration} is {@code null}
     */
    public static String formatDuration(final Duration duration) {
        if (duration == null) {
            return null;
        }
        final Duration positive = duration.isNegative() ? Duration.ZERO : duration;

        final long days = positive.toDays();
        final long hours = positive.toHoursPart();
        final long minutes = positive.toMinutesPart();
        final long seconds = positive.toSecondsPart();

        if (days > 0) {
            return hours > 0 ? days + "d " + hours + "h" : days + "d";
        }
        if (hours > 0) {
            return minutes > 0 ? hours + "h " + minutes + "m" : hours + "h";
        }
        if (minutes > 0) {
            return seconds > 0 ? minutes + "m " + seconds + "s" : minutes + "m";
        }
        return seconds + "s";
    }
}
