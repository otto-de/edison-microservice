package de.otto.edison.example.status;

import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static de.otto.edison.status.domain.StatusDetail.*;
import static java.time.LocalTime.now;
import static java.util.Collections.singletonList;

/**
 * Example for a StatusDetailIndicator that is using the current time to determine the status
 * of the application.
 *
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Component
public class WorkTimeStatusIndicator implements StatusDetailIndicator {

    @Override
    public List<StatusDetail> statusDetails() {
        if (isWorkingTime(now())) {
            return singletonList(statusDetail("Time to work", OK, "go ahead"));
        } else {
            return singletonList(statusDetail("Time to work", WARNING, "go home now"));
        }
    }

    private boolean isWorkingTime(LocalTime time) {
        return time.getHour() >= 9 && time.getHour() < 18;
    }

}
