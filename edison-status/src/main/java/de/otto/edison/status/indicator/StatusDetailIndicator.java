package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.StatusDetail;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
public interface StatusDetailIndicator {

    StatusDetail statusDetail();

    default List<StatusDetail> statusDetails() {
        return asList(statusDetail());
    }

}
