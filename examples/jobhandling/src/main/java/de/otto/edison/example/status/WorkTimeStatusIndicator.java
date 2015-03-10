package de.otto.edison.example.status;

import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static java.time.LocalTime.now;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Component
public class WorkTimeStatusIndicator implements StatusDetailIndicator {

    @Override
    public StatusDetail statusDetail() {
        if (isWorkingTime(now())) {
            return StatusDetail.statusDetail("Time to work", OK, "go ahead");
        } else {
            return StatusDetail.statusDetail("Time to work", WARNING, "go home now");
        }
    }

    private boolean isWorkingTime(LocalTime time) {
        return time.getHour() >= 9 && time.getHour() < 18;
    }

}
