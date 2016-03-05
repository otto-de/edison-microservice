package de.otto.edison.metrics.load;

import com.codahale.metrics.MetricRegistry;
import de.otto.edison.annotations.Beta;
import de.otto.edison.metrics.configuration.MetricsLoadProperties;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * No matter what, the application is in well balanced state,
 * and will not signal any under- or over-load.
 */
@Beta
public class EverythingFineStrategy implements LoadDetector {

    private static final Logger LOG = getLogger(EverythingFineStrategy.class);

    @Override
    public void initialize(MetricRegistry metricRegistry, MetricsLoadProperties properties) {
        LOG.info("Initialize 'everything fine' strategy");
    }

    @Override
    public Status getStatus() {
        return Status.BALANCED;
    }

}
