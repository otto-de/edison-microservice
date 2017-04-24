package de.otto.edison.loggers;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication(scanBasePackages = "de.otto.edison")
public class TestServer {

    public static void main(String[] args) {
        run(TestServer.class, args);
    }

}
