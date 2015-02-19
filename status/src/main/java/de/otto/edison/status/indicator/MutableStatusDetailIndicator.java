package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.StatusDetail;
import net.jcip.annotations.ThreadSafe;

import static java.util.Objects.requireNonNull;

@ThreadSafe
public final class MutableStatusDetailIndicator implements StatusDetailIndicator {

    private volatile StatusDetail statusDetail;

    public MutableStatusDetailIndicator(final StatusDetail initialStatusDetail) {
        this.statusDetail = requireNonNull(initialStatusDetail, "Initial StatusDetail must not be null");
    }

    @Override
    public StatusDetail statusDetail() {
        return statusDetail;
    }

    public void update(final StatusDetail statusDetail) {
        if (!this.statusDetail.getName().equals(statusDetail.getName())) {
            throw new IllegalArgumentException("Must not update StatusDetail with different names. That would be confusing.");
        }
        this.statusDetail = requireNonNull(statusDetail, "Parameter StatusDetail must not be null");
    }
}
