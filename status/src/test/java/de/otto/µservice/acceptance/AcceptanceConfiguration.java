package de.otto.µservice.acceptance;

import de.otto.µservice.status.domain.Status;
import de.otto.µservice.status.indicator.MutableStatusDetailIndicator;
import de.otto.µservice.status.indicator.StatusDetailIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static de.otto.µservice.status.domain.StatusDetail.statusDetail;

/**
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Configuration
public class AcceptanceConfiguration {

    @Bean
    StatusDetailIndicator fooIndicator() {
        return new MutableStatusDetailIndicator(statusDetail("foo", Status.OK, "test ok"));
    }

    @Bean
    StatusDetailIndicator barIndicator() {
        return new MutableStatusDetailIndicator(statusDetail("bar", Status.WARNING, "test warning"));
    }

}
