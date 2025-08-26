package de.otto.edison.example.service;

import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.stereotype.Service;

import java.util.List;

import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * An example for a service that is implementing StatusDetailIndicator.
 *
 * By doing this, the current status of the service can be exposed on the status page.
 *
 * @author Guido Steinacker
 * @since 06.03.15
 */
@Service
public class HelloService implements StatusDetailIndicator {

    public String getMessage() {
        return "Hello Edison Microservice!";
    }

    @Override
    public List<StatusDetail> statusDetails() {
        return singletonList(
                statusDetail("HelloService", Status.OK, "up and running", singletonMap("foo", "bar"))
        );
    }
}
