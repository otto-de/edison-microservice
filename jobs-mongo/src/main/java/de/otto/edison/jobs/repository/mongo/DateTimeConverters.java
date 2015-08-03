package de.otto.edison.jobs.repository.mongo;

import java.time.OffsetDateTime;
import java.util.Date;

import static java.time.OffsetDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;
import static java.util.Date.from;

/**
 * Utility class used to convert java.time.* to and from mongoDB documents.
 *
 * @author Guido Steinacker
 * @since 20.07.15
 */
public class DateTimeConverters {
    private DateTimeConverters() {}

    public static Date toDate(final OffsetDateTime offsetDateTime) {
        return from(offsetDateTime.toInstant());
    }

    public static OffsetDateTime toOffsetDateTime(final Date date) {
        return date == null ? null : ofInstant(date.toInstant(), systemDefault());
    }


}
