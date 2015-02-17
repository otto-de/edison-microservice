package de.otto.µservice.status.domain;

import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@Immutable
public final class ApplicationStatus {

    private final String name;
    private final Status status;
    private final List<StatusDetail> statusDetails;

    private ApplicationStatus(final String applicationName,
                              final List<StatusDetail> details) {
        this.name = applicationName;
        this.status = details.stream()
                .map(StatusDetail::getStatus)
                .reduce(Status.OK, Status::plus);
        this.statusDetails = unmodifiableList(new ArrayList<>(details));
    }

    public static ApplicationStatus detailedStatus(final String applicationName,
                                                   final List<StatusDetail> details) {
        return new ApplicationStatus(applicationName, details);
    }

    public String getName() {
        return name;
    }

    public Status getStatus() {
        return status;
    }

    public List<StatusDetail> getStatusDetails() {
        return statusDetails;
    }

}
