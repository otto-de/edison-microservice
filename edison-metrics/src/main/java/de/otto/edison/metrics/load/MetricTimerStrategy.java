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
public class MetricTimerStrategy implements LoadDetector {

    private static final Logger LOG = getLogger(MetricTimerStrategy.class);

    private final MetricRegistry metricRegistry;

    private final String timerName;

    private final double minThreshold;

    private final double maxThreshold;

    public MetricTimerStrategy(MetricRegistry metricRegistry, String timerName,
                               double minThreshold, double maxThreshold) {
        LOG.info("Initialize metric timer strategy, listening on timer: {}", timerName);
        this.metricRegistry = metricRegistry;
        this.timerName = timerName;
        this.minThreshold = minThreshold;
        this.maxThreshold = maxThreshold;
    }

    @Override
    public Status getStatus() {
        Status result = Status.BALANCED;
        Map<String, Timer> timers = metricRegistry.getTimers((name, metric) -> name.contains(timerName));
        LOG.debug("Retrieved {} timer(s) for {}", timers.size(), timerName);
        if (timers.size() > 0) {
            String firstKey = timers.keySet().iterator().next();
            double curValue = timers.get(firstKey).getOneMinuteRate();
            LOG.info("Timer '{}' has current value: {}", firstKey, curValue);
            if (curValue < minThreshold) {
                result = Status.IDLE;
            } else if (curValue > maxThreshold) {
                result = Status.OVERLOAD;
            }
        } else {
            LOG.warn("Unable to find metrics for timer: {}", timerName);
        }
        return result;
    }
}
