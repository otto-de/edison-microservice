package de.otto.edison.jobs.configuration;

import com.mongodb.MongoClient;
import de.otto.edison.jobs.repository.mongo.MongoJobRepository;
import de.otto.edison.mongo.configuration.MongoConfiguration;
import de.otto.edison.testsupport.mongo.EmbeddedMongoHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class MongoJobsConfigurationTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @ImportAutoConfiguration({MongoConfiguration.class, MongoJobsConfiguration.class})
    private static class JobsAutoConfiguration {
    }

    @BeforeAll
    public static void setupMongo() throws IOException {
        EmbeddedMongoHelper.startMongoDB();
    }

    @AfterAll
    public static void teardownMongo() {
        EmbeddedMongoHelper.stopMongoDB();
    }

    @AfterEach
    void close() {
        this.context.close();
    }
    
    static class MongoTestConfiguration {
        @Bean
        public MongoClient mongoClient() {
            return EmbeddedMongoHelper.getMongoClient();
        }

    }

    @Test
    public void shouldUseMongoJobRepositoryIfEnabled() {
        this.context.register(MongoTestConfiguration.class);
        this.context.register(JobsAutoConfiguration.class);
        TestPropertyValues
                .of("edison.jobs.mongo.enabled=true")
                .and("edison.mongo.db=db")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("jobRepository"), is(true));
        assertThat(this.context.getBean("jobRepository"), is(instanceOf(MongoJobRepository.class)));

    }

}