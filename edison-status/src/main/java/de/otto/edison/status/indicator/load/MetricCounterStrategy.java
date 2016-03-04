package de.otto.edison.status.indicator.load;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import de.otto.edison.annotations.Beta;
import org.slf4j.Logger;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Checks the {@link MetricRegistry} for a given counter and evaluates
 * with a minimum and maxium threshold within it is considered that the
 * application is balanced well, otherwise signal idle resp. overload.
 *
 * @author Niko Schmuck
 */
@Beta
public class MetricCounterStrategy implements LoadDetector {

    private static final Logger LOG = getLogger(MetricCounterStrategy.class);

    private final MetricRegistry metricRegistry;

    private final String counterName;

    private final double minThreshold;

    private final double maxThreshold;

    public MetricCounterStrategy(MetricRegistry metricRegistry, String counterName,
                                 double minThreshold, double maxThreshold) {
        LOG.info("Initialize metric counter strategy, listening on counter: {}", counterName);
        this.metricRegistry = metricRegistry;
        this.counterName = counterName;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    @Override
    public Status getStatus() {
        Status result = Status.BALANCED;
        Map<String, Timer> timers = metricRegistry.getTimers((name, metric) -> name.contains(counterName));
        LOG.debug("Retrieved {} counter(s) for {}", timers.size(), counterName);
        if (timers.size() > 0) {
            String firstKey = timers.keySet().iterator().next();
            double curValue = timers.get(firstKey).getOneMinuteRate();
            LOG.info("Counter '{}' has current value: {}", firstKey, curValue);
            if (curValue < minThreshold) {
                result = Status.IDLE;
            } else if (curValue > maxThreshold) {
                result = Status.OVERLOAD;
            }
        } else {
            LOG.warn("Unable to find metrics for counter: {}", counterName);
        }
        return result;
    }
}
