package de.otto.edison.hystrix.configuration;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers HystrixMetricsStreamServlet
 *
 * @author Guido Steinacker
 * @since 15.04.15
 */
@Configuration
public class HystrixConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServletRegistrationBean hystrixMetricsStreamServlet(){
        return new ServletRegistrationBean(new HystrixMetricsStreamServlet(),"/internal/hystrix.stream");
    }

}
