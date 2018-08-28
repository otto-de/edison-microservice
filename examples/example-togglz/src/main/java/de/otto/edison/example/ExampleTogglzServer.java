package de.otto.edison.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.boot.SpringApplication.run;

@EnableScheduling
@PropertySource("version.properties")
@SpringBootApplication(scanBasePackages = "de.otto.edison")
public class ExampleTogglzServer {

    public static void main(String[] args) {
        run(ExampleTogglzServer.class, args);
    }

}
