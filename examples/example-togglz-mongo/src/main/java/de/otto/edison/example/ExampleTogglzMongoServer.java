package de.otto.edison.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication(scanBasePackages = "de.otto.edison")
@EnableScheduling
@PropertySource("version.properties")
public class ExampleTogglzMongoServer {

    public static void main(String[] args) {
        run(ExampleTogglzMongoServer.class, args);
    }

}
