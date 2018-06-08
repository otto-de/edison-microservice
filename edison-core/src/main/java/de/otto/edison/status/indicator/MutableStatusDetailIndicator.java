package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.StatusDetail;
import net.jcip.annotations.ThreadSafe;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

@ThreadSafe
public class MutableStatusDetailIndicator implements StatusDetailIndicator {

    private volatile StatusDetail statusDetail;

    public MutableStatusDetailIndicator(final StatusDetail initialStatusDetail) {
        this.statusDetail = requireNonNull(initialStatusDetail, "Initial StatusDetail must not be null");
    }

    @Override
    public List<StatusDetail> statusDetails() {
        return singletonList(statusDetail);
    }

    public void update(final StatusDetail statusDetail) {
        if (!this.statusDetail.getName().equals(statusDetail.getName())) {
            throw new IllegalArgumentException("Must not update StatusDetail with different names. That would be confusing.");
        }
        this.statusDetail = requireNonNull(statusDetail, "Parameter StatusDetail must not be null");
    }

    public void toOk(String message) {
        update(statusDetail.toOk(message));
    }

    public void toWarning(String message) {
        update(statusDetail.toWarning(message));
    }

    public void toError(String message) {
        update(statusDetail.toError(message));
    }

    public void withDetail(String key, String value) {
        update(statusDetail.withDetail(key, value));
    }

    public void withoutDetail(String key) {
        update(statusDetail.withoutDetail(key));
    }
}
