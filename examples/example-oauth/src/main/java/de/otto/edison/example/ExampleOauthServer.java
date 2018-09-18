package de.otto.edison.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;

import static org.springframework.boot.SpringApplication.run;

@PropertySource("version.properties")
@SpringBootApplication(scanBasePackages = "de.otto.edison")
@EnableScheduling
public class ExampleOauthServer {

    public static void main(String[] args) {
        run(ExampleOauthServer.class, args);
    }

}
