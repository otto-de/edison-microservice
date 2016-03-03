package de.otto.edison.status.indicator.load;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Checks the {@link MetricRegistry} for a given counter and evaluates
 * with a minimum and maxium threshold within it is considered that the
 * application is balanced well, otherwise signal idle resp. overload.
 *
 * @author Niko Schmuck
 */
public class MetricCounterStrategy implements LoadDetector {

    private static final Logger LOG = getLogger(MetricCounterStrategy.class);

    private final MetricRegistry metricRegistry;

    private final String counterName;

    private final long minThreshold;

    private final long maxThreshold;

    public MetricCounterStrategy(MetricRegistry metricRegistry, String counterName,
                                 long minThreshold, long maxThreshold) {
        LOG.info("Initialize metric counter strategy, listening on {}", counterName);
        this.metricRegistry = metricRegistry;
        this.counterName = counterName;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    @Override
    public Status getStatus() {
        Status result = Status.BALANCED;
        LOG.info("----> Counters: {}", metricRegistry.getCounters());
        Map<String, Counter> counters = metricRegistry.getCounters((name, metric) -> name.contains(counterName));
        LOG.info("Retrieved {} counter(s) for {}", counters.size(), counterName);
        if (counters.size() > 0) {
            String firstKey = counters.keySet().iterator().next();
            long curValue = counters.get(firstKey).getCount();
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
