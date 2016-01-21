package de.otto.edison.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@ComponentScan(basePackages = {"de.otto.edison"})
public class ExampleServer {

    public static void main(String[] args) {
        run(ExampleServer.class, args);
    }

}
