package de.otto.edison.example;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

import static org.springframework.boot.SpringApplication.run;

@PropertySource("version.properties")
@SpringBootApplication

// As this library uses AutoConfigurations, you do NOT need to scan its classpath for components.
// If your application uses '@SpringBootApplication(scanBasePackages = "de.otto")' or comparable,
// you should be fine as it excludes AutoConfigurations from ComponentScan.
// If you have to use '@ComponentScan', make sure to set AutoConfigurationExcludeFilter as follows:
//@ComponentScan(basePackages = "de.otto", excludeFilters = {
//        @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
//})
public class ExampleStatusServer {

    public static void main(String[] args) {
        run(ExampleStatusServer.class, args);
    }

}
