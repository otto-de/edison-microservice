package de.otto.edison.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import static org.springframework.boot.SpringApplication.run;

@PropertySource("version.properties")
@SpringBootApplication(scanBasePackages = "de.otto.edison")
public class ExampleStatusServer {

    public static void main(String[] args) {
        run(ExampleStatusServer.class, args);
    }

}
