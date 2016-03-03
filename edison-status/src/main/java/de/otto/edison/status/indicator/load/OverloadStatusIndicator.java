package de.otto.edison.status.indicator.load;

import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;

@Component
public class OverloadStatusIndicator implements StatusDetailIndicator {

    @Autowired
    private OverloadDetector overloadDetector;

    @Override
    public StatusDetail statusDetail() {
        return StatusDetail.statusDetail("overload",
                overloadDetector.isOverloaded() ? WARNING : OK, "detects whether application is under heavy load.");
    }

}
