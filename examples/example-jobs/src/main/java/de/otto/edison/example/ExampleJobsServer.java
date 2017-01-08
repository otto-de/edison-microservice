package de.otto.edison.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableScheduling
@PropertySource("version.properties")
@ComponentScan("de.otto.edison")
public class ExampleJobsServer {

    public static void main(String[] args) {
        run(ExampleJobsServer.class, args);
    }

}
