package de.otto.edison.aws.s3.togglz;

import de.otto.edison.aws.s3.configuration.S3TogglzProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

import java.net.URI;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;

@Ignore
public class S3StateRepositoryTest {

    private S3Client client;
    private S3StateRepository repository;

    @Before
    public void setup() {
        client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("test", "test")))
                .endpointOverride(URI.create("http://localhost:4572"))
                .region(Region.EU_CENTRAL_1)
                .build();

        // TODO: Delete bucket instead of creating a new one on every run:
        final String bucket = "test-togglz-" + System.currentTimeMillis();
        client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());

        final S3TogglzProperties togglzProperties = new S3TogglzProperties();
        togglzProperties.setBucketName(bucket);
        repository = new S3StateRepository(togglzProperties, client);
    }

    @Test
    public void testGetSetFeatureState() {
        assertNull(repository.getFeatureState(TestFeature.FEATURE_1));

        final FeatureState initState = new FeatureState(TestFeature.FEATURE_1)
                .setEnabled(true)
                .setStrategyId("abc")
                .setParameter("key1", "value1");

        repository.setFeatureState(initState);

        final FeatureState actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualState.getFeature()).isEqualTo(initState.getFeature());
        assertThat(actualState.getStrategyId()).isEqualTo("abc");
        assertThat(actualState.isEnabled()).isEqualTo(true);
        assertThat(actualState.getParameter("key1")).isEqualTo("value1");
        assertThat(actualState.getParameterNames()).isEqualTo(new HashSet<String>() {
            {
                add("key1");
            }
        });
    }

    @Test
    public void testUpdateFeatureState() {
        final FeatureState initState = new FeatureState(TestFeature.FEATURE_1)
                .setEnabled(true)
                .setStrategyId("abc")
                .setParameter("key1", "value1");

        repository.setFeatureState(initState);

        FeatureState actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualState.getFeature()).isEqualTo(initState.getFeature());

        final FeatureState updatedState = new FeatureState(TestFeature.FEATURE_1)
                .setEnabled(false)
                .setStrategyId("def")
                .setParameter("key2", "value2");

        repository.setFeatureState(updatedState);

        actualState = repository.getFeatureState(TestFeature.FEATURE_1);

        assertThat(actualState.getFeature()).isEqualTo(initState.getFeature());
        assertThat(actualState.getStrategyId()).isEqualTo("def");
        assertThat(actualState.isEnabled()).isEqualTo(false);
        assertThat(actualState.getParameter("key2")).isEqualTo("value2");
        assertThat(actualState.getParameterNames()).isEqualTo(new HashSet<String>() {
            {
                add("key2");
            }
        });
    }

    private enum TestFeature implements Feature {
        FEATURE_1
    }

}
