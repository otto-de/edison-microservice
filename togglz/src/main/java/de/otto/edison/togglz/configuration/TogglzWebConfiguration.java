package de.otto.edison.togglz.configuration;


import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;
import org.togglz.servlet.TogglzFilter;

@Configuration
public class TogglzWebConfiguration {

    @Bean
    public FilterRegistrationBean togglzFilter() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new TogglzFilter());
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }

    @Bean
    public ServletRegistrationBean togglzServlet() {
        return new ServletRegistrationBean(new TogglzConsoleServlet(), "/internal/togglz/*");
    }
}
