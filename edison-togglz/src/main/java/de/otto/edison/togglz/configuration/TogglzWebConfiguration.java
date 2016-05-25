package de.otto.edison.togglz.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.servlet.TogglzFilter;

@Configuration
public class TogglzWebConfiguration {

    @Bean
    @ConditionalOnMissingBean(name="togglzFilter")
    public FilterRegistrationBean togglzFilter() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new TogglzFilter());
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }

    @Bean
    public ServletRegistrationBean togglzServlet(@Value("${management.context-path:/internal}") String prefix) {
        return new ServletRegistrationBean(new TogglzConsoleServlet(), prefix + "/togglz/*");
    }
}
