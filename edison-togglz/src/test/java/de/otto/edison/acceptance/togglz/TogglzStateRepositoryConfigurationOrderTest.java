package de.otto.edison.acceptance.togglz;

import com.mongodb.client.MongoDatabase;
import de.otto.edison.mongo.configuration.MongoProperties;
import de.otto.edison.togglz.FeatureClassProvider;
import de.otto.edison.togglz.configuration.InMemoryFeatureStateRepositoryConfiguration;
import de.otto.edison.togglz.configuration.MongoTogglzConfiguration;
import de.otto.edison.togglz.configuration.S3TogglzConfiguration;
import de.otto.edison.togglz.mongo.MongoTogglzRepository;
import de.otto.edison.togglz.s3.S3TogglzRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.UserProvider;
import software.amazon.awssdk.services.s3.S3Client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class TogglzStateRepositoryConfigurationOrderTest {

    private final ApplicationContextRunner baseContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    InMemoryFeatureStateRepositoryConfiguration.class,
                    S3TogglzConfiguration.class,
                    MongoTogglzConfiguration.class
            ));

    @Test
    void mongoStateRepositoryIsRegisteredInsteadOfInMemory() {
        baseContextRunner
                .withPropertyValues("edison.togglz.mongo.enabled=true")
                .withBean(MongoDatabase.class, () -> mock(MongoDatabase.class))
                .withBean(FeatureClassProvider.class, () -> mock(FeatureClassProvider.class))
                .withBean(UserProvider.class, () -> mock(UserProvider.class))
                .withBean(MongoProperties.class, MongoProperties::new)
                .run(context -> {
                    assertThat(context.getBean(StateRepository.class), is(instanceOf(MongoTogglzRepository.class)));
                });
    }

    @Test
    void s3StateRepositoryIsRegisteredInsteadOfInMemory() {
        baseContextRunner
                .withPropertyValues(
                        "edison.togglz.s3.enabled=true",
                        "edison.togglz.s3.bucket-name=test-bucket")
                .withBean(S3Client.class, () -> mock(S3Client.class))
                .withBean(FeatureClassProvider.class, () -> mock(FeatureClassProvider.class))
                .withBean(UserProvider.class, () -> mock(UserProvider.class))
                .run(context -> {
                    assertThat(context.getBean(StateRepository.class), is(instanceOf(S3TogglzRepository.class)));
                });
    }

    @Test
    void inMemoryStateRepositoryIsUsedAsFallback() {
        baseContextRunner
                .run(context -> {
                    assertThat(context.getBean(StateRepository.class), is(instanceOf(InMemoryStateRepository.class)));
                });
    }
}
