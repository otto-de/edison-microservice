package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * A StatusDetailIndicator that is a composite of multiple delegates.
 *
 * @author Guido Steinacker
 * @since 04.09.15
 */
public class CompositeStatusDetailIndicator implements StatusDetailIndicator {

    private final List<StatusDetailIndicator> delegates;

    public CompositeStatusDetailIndicator(final List<StatusDetailIndicator> delegates) {
        this.delegates = delegates;
        if (delegates == null || delegates.isEmpty()) {
            throw new IllegalArgumentException("CompositeStatusDetailIndicator does not have any delegate indicators");
        }
    }

    @Override
    public List<StatusDetail> statusDetails() {
        return delegates
                .stream()
                .map(StatusDetailIndicator::statusDetails)
                .flatMap(Collection::stream)
                .collect(toList());
    }
}
