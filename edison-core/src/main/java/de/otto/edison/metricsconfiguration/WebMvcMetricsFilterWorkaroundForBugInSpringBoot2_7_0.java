package de.otto.edison.metricsconfiguration;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsProperties;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;

@Component
public class WebMvcMetricsFilterWorkaroundForBugInSpringBoot2_7_0 {

    @Bean
    public FilterRegistrationBean<WebMvcMetricsFilter> webMvcMetricsFilter(MetricsProperties properties,
                                                                           MeterRegistry registry, WebMvcTagsProvider tagsProvider) {
        MetricsProperties.Web.Server.ServerRequest request = properties.getWeb().getServer().getRequest();
        WebMvcMetricsFilter filter = new WebMvcMetricsFilter(registry, tagsProvider, request.getMetricName(),
                request.getAutotime());
        FilterRegistrationBean<WebMvcMetricsFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        return registration;
    }

}
