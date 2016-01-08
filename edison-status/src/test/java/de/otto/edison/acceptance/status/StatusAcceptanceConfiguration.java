package de.otto.edison.acceptance.status;

import de.otto.edison.status.domain.Status;
import de.otto.edison.status.indicator.MutableStatusDetailIndicator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.edison.status.domain.StatusDetail.statusDetail;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Configuration
public class StatusAcceptanceConfiguration {

    @Bean
    StatusDetailIndicator fooIndicator() {
        return new MutableStatusDetailIndicator(statusDetail("foo", Status.OK, "test ok"));
    }

    @Bean
    StatusDetailIndicator barIndicator() {
        return new MutableStatusDetailIndicator(statusDetail("bar", Status.WARNING, "test warning"));
    }

}
