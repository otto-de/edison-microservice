package de.otto.edison.togglz.configuration;

import com.mongodb.client.MongoDatabase;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TogglzConfigurationTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    private final static MongoDatabase mockMongoDatabase = Mockito.mock(MongoDatabase.class);
    private final static S3Client s3Client = Mockito.mock(S3Client.class);

    @AfterEach
    public void close() {
        if (this.context != null) {
            this.context.close();
        }
    }

    @ImportAutoConfiguration({S3TogglzConfiguration.class, TogglzConfiguration.class,
            InMemoryFeatureStateRepositoryConfiguration.class,
            MongoAndS3TestConfiguration.class,
            MongoProperties.class,
            MongoTogglzConfiguration.class})
    private static class MultipleTogglzConfigsAutoConfiguration {
    }

    @TestConfiguration
    static class MongoAndS3TestConfiguration {
        @Bean
        public MongoDatabase mongoDatabase() {
            return mockMongoDatabase;
        }

        @Bean
        public S3Client s3Client() {
            return s3Client;
        }
    }

    @Test
    public void shouldRegisterTogglzConsoleServlet() {
        this.context.register(TogglzConfiguration.class);
        this.context.register(InMemoryFeatureStateRepositoryConfiguration.class);
        TestPropertyValues.of("edison.application.management.base-path=/internal").applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("togglzFilter"), is(true));
        assertThat(this.context.containsBean("featureClassProvider"), is(true));
        assertThat(this.context.containsBean("userProvider"), is(true));
        assertThat(this.context.containsBean("togglzConfig"), is(true));
        assertThat(this.context.containsBean("featureManager"), is(true));
    }

    @Test
    public void shouldThrowExceptionOnMultipleBackendConfigs() {
        //given
        this.context.register(MultipleTogglzConfigsAutoConfiguration.class);
        this.context.register(MongoAndS3TestConfiguration.class);

        TestPropertyValues
                .of("edison.togglz.s3.enabled=true")
                .and("edison.togglz.s3.bucket-name=togglz-bucket")
                .and("edison.togglz.s3.check-bucket=true")
                .and("edison.mongo.db=db")
                .and("edison.mongo.user=test")
                .and("edison.mongo.password=test")
                .applyTo(context);

        //expect
        Assertions.assertThrows(BeanCreationException.class, this.context::refresh);
    }

    @Test
    public void shouldAllowSingleBackendConfig() {
        //given
        this.context.register(MultipleTogglzConfigsAutoConfiguration.class);
        this.context.register(MongoAndS3TestConfiguration.class);

        TestPropertyValues
                .of("edison.togglz.s3.enabled=true")
                .and("edison.togglz.s3.bucket-name=togglz-bucket")
                .and("edison.togglz.s3.check-bucket=true")
                .and("edison.togglz.mongo.enabled=false")
                .and("edison.mongo.db=db")
                .and("edison.mongo.user=test")
                .and("edison.mongo.password=test")
                .applyTo(context);

        //expect
        this.context.refresh();
    }

}
