package de.otto.edison.metrics.load;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import de.otto.edison.annotations.Beta;
import org.slf4j.Logger;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Checks the {@link MetricRegistry} for a given {@link com.codahale.metrics.Timer}
 * and evaluates with a minimum and maxium threshold within which it is considered
 * that the application is balanced well, otherwise signal idle resp. overload.
 *
 * @author Niko Schmuck
 */
@Beta
public class MetricsTimerStrategy implements LoadDetector {

    private static final Logger LOG = getLogger(MetricsTimerStrategy.class);

    private MetricRegistry metricRegistry;
    private MetricsLoadProperties properties;

    @Override
    public Status getStatus() {
        Status result = Status.BALANCED;
        Map<String, Timer> timers = metricRegistry.getTimers((name, metric) -> name.contains(properties.getTimerName()));
        LOG.debug("Retrieved {} timer(s) for {}", timers.size(), properties.getTimerName());
        if (timers.size() > 0) {
            String firstKey = timers.keySet().iterator().next();
            double curValue = timers.get(firstKey).getOneMinuteRate();
            LOG.info("Timer '{}' has current value: {}", firstKey, curValue);
            if (curValue < properties.getMinThreshold()) {
                result = Status.IDLE;
            } else if (curValue > properties.getMaxThreshold()) {
                result = Status.OVERLOAD;
            }
        } else {
            LOG.warn("Unable to find metrics for timer: {}", properties.getTimerName());
        }
        return result;
    }

    @Override
    public void initialize(MetricRegistry metricRegistry, MetricsLoadProperties properties) {
        LOG.info("Initialize metric timer strategy, listening on timer: {}", properties.getTimerName());
        this.metricRegistry = metricRegistry;
        this.properties = properties;
    }

}
