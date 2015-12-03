package de.otto.edison.testsupport;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.springframework.boot.SpringApplication.run;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"de.otto.edison"})
public class TestServer {

    private static ApplicationContext ctx;

    public static ApplicationContext applicationContext() {
        return ctx;
    }

    public static void main(String[] args) {
        ctx = run(TestServer.class, args);
    }

}
