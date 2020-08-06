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
    private static class TogglzTestConfiguration {
    }

    @ImportAutoConfiguration({MongoConfiguration.class, TogglzConfiguration.class, MongoTogglzConfiguration.class, InMemoryFeatureStateRepositoryConfiguration.class})
    private static class MongoTogglzTestConfiguration {
    }

    @AfterEach
    void close() {
        this.context.close();
    }

    @Test
    void shouldUseMongoStateRepositoryIfEnabled() {
        this.context.register(MongoTogglzTestConfiguration.class);
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
    void shouldUseInMemoryStateRepositoryIfMongoDisabled() {
        this.context.register(MongoTogglzTestConfiguration.class);
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
    void shouldUseInMemoryStateRepositoryIfMissingMongoClient() {
        this.context.register(TogglzTestConfiguration.class);
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