package de.otto.edison.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableScheduling
@PropertySource("version.properties")

// As this library uses AutoConfigurations, you do NOT need to scan its classpath for components.
// If your application uses '@SpringBootApplication(scanBasePackages = "de.otto")' or comparable,
// you should be fine as it excludes AutoConfigurations from ComponentScan.
// If you have to use '@ComponentScan', make sure to set AutoConfigurationExcludeFilter as follows:
//@ComponentScan(basePackages = "de.otto", excludeFilters = {
//        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
//})
public class ExampleTogglzMongoServer {

    public static void main(String[] args) {
        run(ExampleTogglzMongoServer.class, args);
    }

}
