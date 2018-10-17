package de.otto.edison.togglz.s3;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.core.signer.NoOpSigner;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

public class S3TestHelper {
    public static S3Client createS3Client(final Integer mappedPort) {
        final AwsBasicCredentials credentials = AwsBasicCredentials.create("test", "test");
        final StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        return S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .endpointOverride(URI.create(String.format("http://localhost:%d", mappedPort)))
                .region(Region.EU_CENTRAL_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .overrideConfiguration(ClientOverrideConfiguration
                        .builder()
                        .putAdvancedOption(SdkAdvancedClientOption.SIGNER, new NoOpSigner())
                        .build())
                .build();
    }

    public static GenericContainer<?> createTestContainer(final int testPort) {
        return new GenericContainer<>("localstack/localstack:latest")
                .withEnv("DEBUG", "1")
                .waitingFor(new HttpWaitStrategy().forStatusCode(200).forPort(testPort));
    }


}
