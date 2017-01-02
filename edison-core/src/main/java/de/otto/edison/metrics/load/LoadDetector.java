package de.otto.edison.metrics.load;

import com.codahale.metrics.MetricRegistry;
import de.otto.edison.annotations.Beta;

/**
 * Implementation of this interface allow to steer whether the application
 * is able to run smoothly versus nearly unable to handle the load.
 */
@Beta
public interface LoadDetector {

    void initialize(MetricRegistry metricRegistry, MetricsLoadProperties properties);

    enum Status {

        /**
         * Application has less load, therefore for example the cluster might
         * decide to scale down the number of instances of this application.
         */
        IDLE,

        /**
         * Application is acting fine, no need to react for auto-scaling needs
         * for example.
         */
        BALANCED,

        /**
         * Application is overloaded.
         */
        OVERLOAD

    }

    Status getStatus();

}
