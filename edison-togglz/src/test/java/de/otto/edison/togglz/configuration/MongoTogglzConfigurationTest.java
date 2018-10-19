package de.otto.edison.togglz.configuration;

import de.otto.edison.mongo.configuration.MongoConfiguration;
import de.otto.edison.togglz.mongo.MongoTogglzRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class MongoTogglzConfigurationTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @ImportAutoConfiguration({TogglzConfiguration.class, MongoTogglzConfiguration.class, InMemoryFeatureStateRepositoryConfiguration.class})
    private static class TogglzAutoConfiguration {
    }

    @AfterEach
    void close() {
        this.context.close();
    }

    @Test
    public void shouldUseMongoStateRepositoryIfEnabled() {
        this.context.register(MongoConfiguration.class);
        this.context.register(TogglzAutoConfiguration.class);
        TestPropertyValues
                .of("edison.togglz.mongo.enabled=true")
                .and("edison.mongo.db=db")
                .and("edison.mongo.user=test")
                .and("edison.mongo.password=test")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("stateRepository"), is(true));
        assertThat(this.context.getBean("stateRepository", StateRepository.class), is(instanceOf(MongoTogglzRepository.class)));
    }

    @Test
    public void shouldUseInMemoryStateRepositoryIfMongoDisabled() {
        this.context.register(MongoConfiguration.class);
        this.context.register(TogglzAutoConfiguration.class);
        TestPropertyValues
                .of("edison.togglz.mongo.enabled=false")
                .and("edison.mongo.db=db")
                .and("edison.mongo.user=test")
                .and("edison.mongo.password=test")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("stateRepository"), is(true));
        assertThat(this.context.getBean("stateRepository", StateRepository.class), is(instanceOf(InMemoryStateRepository.class)));
    }

    @Test
    public void shouldUseInMemoryStateRepositoryIfMissingMongoClient() {
        this.context.register(TogglzAutoConfiguration.class);
        TestPropertyValues
                .of("edison.togglz.mongo.enabled=true")
                .and("edison.mongo.db=db")
                .and("edison.mongo.user=test")
                .and("edison.mongo.password=test")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("stateRepository"), is(true));
        assertThat(this.context.getBean("stateRepository", StateRepository.class), is(instanceOf(InMemoryStateRepository.class)));
    }
}