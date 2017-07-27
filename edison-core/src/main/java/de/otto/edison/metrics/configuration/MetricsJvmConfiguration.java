package de.otto.edison.metrics.configuration;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.*;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableMetrics
public class MetricsJvmConfiguration extends MetricsConfigurerAdapter {

    @Override
    public void configureReporters(final MetricRegistry metricRegistry) {
        metricRegistry.register("gc", new GarbageCollectorMetricSet());
        metricRegistry.register("memory", new MemoryUsageGaugeSet());
        metricRegistry.register("filedescriptors.ratio", new FileDescriptorRatioGauge());
        metricRegistry.register("threads", new CachedThreadStatesGaugeSet(10, TimeUnit.SECONDS));
    }

}
