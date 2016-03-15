package de.otto.edison.togglz.configuration;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.togglz.console.TogglzConsoleServlet;

@Configuration
public class TogglzWebConfiguration {
    
    @Bean
    public ServletRegistrationBean togglzServlet(@Value("${management.context-path:/internal}") String prefix) {
        return new ServletRegistrationBean(new TogglzConsoleServlet(), prefix + "/togglz/*");
    }
}
