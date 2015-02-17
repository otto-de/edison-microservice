package de.otto.µservice.example.status;

import de.otto.µservice.status.domain.StatusDetail;
import de.otto.µservice.status.indicator.StatusDetailIndicator;
import org.springframework.stereotype.Component;

import static de.otto.µservice.status.domain.Status.OK;
import static java.lang.String.format;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Component
public class ExampleStatusIndicator implements StatusDetailIndicator {

    private volatile int count;

    public void incGreetings() {
        ++count;
    }

    @Override
    public StatusDetail statusDetail() {
        return StatusDetail.statusDetail("hello", OK, format("%d greetings delivered", count));
    }

}
