package de.otto.edison.status.controller;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.StatusDetail;
import net.jcip.annotations.Immutable;
import org.springframework.format.datetime.joda.LocalDateTimeParser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.FormatStyle.LONG;
import static java.time.format.FormatStyle.MEDIUM;

@Immutable
final class ApplicationStatusRepresentation {


    private static final String SYSTEM_START_TIME = now().format(ofLocalizedDateTime(LONG));

    private final ApplicationStatus applicationStatus;

    private ApplicationStatusRepresentation(final ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public static ApplicationStatusRepresentation statusRepresentationOf(final ApplicationStatus status) {
        return new ApplicationStatusRepresentation(status);
    }

    public Map<String, ?> getApplication() {
        return new LinkedHashMap<String, Object>() {{
            put("status", applicationStatus.getStatus().name());
            put("name", applicationStatus.getName());
            put("hostname", applicationStatus.getHostName());
            put("systemtime", now().format(ofLocalizedDateTime(LONG)));
            put("systemstarttime", SYSTEM_START_TIME);
            put("commit", applicationStatus.getVersionInfo().getCommit());
            put("version", applicationStatus.getVersionInfo().getVersion());
            put("statusDetails", statusDetailsRepresentationOf(applicationStatus));
        }};
    }

    private Map<String, ?> statusDetailsRepresentationOf(final ApplicationStatus applicationStatus) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (StatusDetail entry : applicationStatus.getStatusDetails()) {
            map.put(entry.getName(), new LinkedHashMap<String, String>() {{
                put("status", entry.getStatus().name());
                put("message", entry.getMessage());
                putAll(entry.getDetails());
            }});
        }
        return map;

    }
}
