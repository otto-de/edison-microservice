package de.otto.edison.jobs;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import static org.springframework.boot.SpringApplication.run;

@Configuration
@ComponentScan(basePackages = {"de.otto.edison"}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.otto.edison.mongo.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "de.otto.edison.status.*Controller")
})
@SpringBootApplication
public class TestServer {

    private static ApplicationContext ctx;

    public static ApplicationContext applicationContext() {
        return ctx;
    }

    public static void main(String[] args) {
        ctx = run(TestServer.class, args);
    }

}
