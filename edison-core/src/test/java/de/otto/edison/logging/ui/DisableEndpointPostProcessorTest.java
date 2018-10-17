package de.otto.edison.logging.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DisableEndpointPostProcessorTest {

    private AnnotationConfigApplicationContext ctx;

    @BeforeEach
    public void setup() {
        ctx = new AnnotationConfigApplicationContext();
    }

    @AfterEach
    public void close() {
        if (ctx != null) {
            ctx.close();
        }
    }

    @Test
    public void shouldRegisterBean() {
        ctx.register(TestEndpointConfiguration.class);
        ctx.refresh();
        assertThat(ctx.containsBean("someTestMvcEndpoint"), is(true));
    }

    @Test
    public void shouldDisableEndpoint() {
        TestPropertyValues.of("endpoints.someTest.enabled=true").applyTo(ctx);
        ctx.register(TestEndpointConfiguration.class);
        ctx.register(RemoveTestEndpointConfiguration.class);
        ctx.refresh();
        assertThat(ctx.getEnvironment().getProperty("endpoints.someTest.enabled"), is("false"));
    }

    @Configuration
    static class TestEndpointConfiguration {
        @Bean
        Object someTestMvcEndpoint() {
            return new Object();
        }
    }

    @Configuration
    static class RemoveTestEndpointConfiguration {
        @Bean
        DisableEndpointPostProcessor withoutSomeBean() {
            return new DisableEndpointPostProcessor("someTest");
        }
    }

}