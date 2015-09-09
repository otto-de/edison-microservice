package de.otto.edison.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.togglz.core.context.StaticFeatureManagerProvider;
import org.togglz.core.manager.FeatureManager;

@Configuration
@EnableAutoConfiguration
@EnableScheduling
@PropertySource("version.properties")
@ComponentScan({"de.otto.edison"})
public class Server {

    private static ApplicationContext ctx;

    public static ApplicationContext applicationContext() {
        return ctx;
    }

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(Server.class);
        ctx = springApplication.run(args);
        FeatureManager manager = ctx.getBean(FeatureManager.class);
        StaticFeatureManagerProvider.setFeatureManager(manager);  // this workaround should be fixed with togglz version 2.2
    }

}