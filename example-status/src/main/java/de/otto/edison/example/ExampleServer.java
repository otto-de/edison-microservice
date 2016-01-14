package de.otto.edison.example;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static org.springframework.boot.SpringApplication.run;

@Configuration
@EnableAutoConfiguration
@ComponentScan(basePackages = {"de.otto.edison"})
@PropertySource("version.properties")
public class ExampleServer {

    public static void main(String[] args) {
        run(ExampleServer.class, args);
    }

}
