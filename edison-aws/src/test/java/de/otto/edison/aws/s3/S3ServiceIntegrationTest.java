package de.otto.edison.aws.s3;

import de.otto.edison.aws.configuration.AwsConfiguration;
import de.otto.edison.aws.s3.S3Service;
import de.otto.edison.aws.s3.configuration.S3Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.List;

import static java.nio.file.Files.createTempFile;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AwsConfiguration.class, S3Configuration.class})
@TestPropertySource("classpath:application-test.properties")
@Ignore
public class S3ServiceIntegrationTest {

    @ClassRule
    public final static GenericContainer localstackContainer = new FixedHostPortGenericContainer("localstack/localstack:latest")
            .withFixedExposedPort(4572, 4572)
            .withNetworkMode("host");

    private static final String TESTBUCKET = "testbucket";

    private S3Service s3Service;

    @Before
    public void setUp() throws Exception {

        S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .region(Region.US_EAST_1)
                .endpointOverride(new URI("http://localhost:4572"))
                .serviceConfiguration(software.amazon.awssdk.services.s3.S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();

        s3Service = new S3Service(s3Client);

        s3Service.createBucket(TESTBUCKET);
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);
    }

    @After
    public void tearDown() {
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);
    }

    @Test
    public void shouldOnlyDeleteFilesWithPrefix() throws Exception {
        //given
        final File tempFile = createTempFile("test", ".txt").toFile();
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.append("Hello World!");
            writer.flush();
        }
        s3Service.upload(TESTBUCKET, tempFile);
        final File prefixedTempFile = createTempFile("prefix", ".txt").toFile();
        try (FileWriter writer = new FileWriter(prefixedTempFile)) {
            writer.append("Hello World!");
            writer.flush();
        }
        s3Service.upload(TESTBUCKET, prefixedTempFile);

        System.out.println(prefixedTempFile.getName());

        //when
        s3Service.deleteAllObjectsWithPrefixInBucket(TESTBUCKET, "prefix");

        //then
        List<String> allFiles = s3Service.listAllFiles(TESTBUCKET);
        System.out.println(allFiles);
        assertThat(allFiles, contains(startsWith("test")));
        assertThat(allFiles, not(contains(startsWith("prefixed_test"))));
    }

    @Test
    public void shouldDeleteAllFilesInBucket() throws Exception {
        //given
        s3Service.upload(TESTBUCKET, createTempFile("test", ".json.zip").toFile());
        s3Service.upload(TESTBUCKET, createTempFile("prefixed_test", ".json.zip").toFile());

        //when
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);

        //then
        List<String> allFiles = s3Service.listAllFiles(TESTBUCKET);
        assertThat(allFiles, hasSize(0));
    }

}
