package de.otto.edison.togglz.configuration;

import de.otto.edison.togglz.s3.S3TogglzRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import software.amazon.awssdk.services.s3.S3Client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

class S3TogglzConfigurationTest {

    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @ImportAutoConfiguration({S3TestConfiguration.class, TogglzConfiguration.class, S3TogglzConfiguration.class, InMemoryFeatureStateRepositoryConfiguration.class})
    private static class TogglzAutoConfiguration {
    }

    @AfterEach
    void close() {
        this.context.close();
    }

    @TestConfiguration
    static class S3TestConfiguration {
        @Bean
        public S3Client s3Client() {
            return S3Client.create();
        }
    }

    @Test
    public void shouldExposeS3StaterepositoryIfEnabled() {
        this.context.register(TogglzAutoConfiguration.class);
        this.context.register(S3TestConfiguration.class);
        TestPropertyValues
                .of("edison.togglz.s3.enabled=true")
                .and("edison.togglz.s3.bucket-name=togglz-bucket")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("stateRepository"), is(true));
        assertThat(this.context.getBean("stateRepository", StateRepository.class), is(instanceOf(S3TogglzRepository.class)));
    }

    @Test
    public void shouldUseInMemoryStateRepositoryIfS3BucketNotConfigured() {
        this.context.register(TogglzAutoConfiguration.class);
        this.context.register(S3TestConfiguration.class);
        TestPropertyValues
                .of("edison.togglz.s3.enabled=true")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("stateRepository"), is(true));
        assertThat(this.context.getBean("stateRepository", StateRepository.class), is(instanceOf(InMemoryStateRepository.class)));
    }

    @Test
    public void shouldUseInMemoryStateRepositoryIfS3Disabled() {
        this.context.register(S3TestConfiguration.class);
        this.context.register(TogglzAutoConfiguration.class);
        TestPropertyValues
                .of("edison.togglz.s3.enabled=false")
                .and("edison.togglz.s3.bucket-name=togglz-bucket")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("stateRepository"), is(true));
        assertThat(this.context.getBean("stateRepository", StateRepository.class), is(instanceOf(InMemoryStateRepository.class)));
    }

    @Test
    public void shouldUseInMemoryStateRepositoryIfMissingS3Client() {
        this.context.register(TogglzAutoConfiguration.class);
        TestPropertyValues
                .of("edison.togglz.s3.enabled=true")
                .and("edison.togglz.s3.bucket-name=togglz-bucket")
                .applyTo(context);
        this.context.refresh();

        assertThat(this.context.containsBean("stateRepository"), is(true));
        assertThat(this.context.getBean("stateRepository", StateRepository.class), is(instanceOf(InMemoryStateRepository.class)));
    }


}