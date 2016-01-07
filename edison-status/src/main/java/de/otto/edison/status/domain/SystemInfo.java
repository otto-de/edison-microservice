package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.time.OffsetDateTime;

import static java.time.Duration.between;
import static java.time.OffsetDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Immutable
public class SystemInfo {

    private static final OffsetDateTime START_TIME = now();

    public final String hostname;
    public final int port;

    public SystemInfo(final String hostname, final int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public String getTime() {
        return now().format(ISO_DATE_TIME);
    }

    public String getStartTime() {
        return START_TIME.format(ISO_DATE_TIME);
    }

    public String getUpTime() {
        final long seconds = between(START_TIME, now()).getSeconds();
        return String.format("%d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, (seconds % 60));
    }
}
