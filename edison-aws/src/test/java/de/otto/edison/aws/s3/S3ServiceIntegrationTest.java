package de.otto.edison.aws.s3;

import de.otto.edison.aws.configuration.AwsConfiguration;
import de.otto.edison.testsupport.aws.AwsTestconfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.GenericContainer;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

import static de.otto.edison.aws.s3.S3TestHelper.createS3Client;
import static de.otto.edison.aws.s3.S3TestHelper.createTestContainer;
import static java.nio.file.Files.createTempFile;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AwsTestconfiguration.class, AwsConfiguration.class})
@ActiveProfiles("test")
public class S3ServiceIntegrationTest {

    private static final int TEST_PORT_S3 = 4572;
    private static final String TESTBUCKET = "testbucket";

    private final static GenericContainer<?> localstackContainer = createTestContainer(TEST_PORT_S3);

    private S3Service s3Service;

    @BeforeAll
    public static void prepareContext() {
        // Set for AWS SDK
        System.setProperty("aws.region", "eu-central-1");
        localstackContainer.start();
    }

    @AfterAll
    public static void stopContainer() {
        localstackContainer.stop();
    }

    @BeforeEach
    public void setUp() {
        final Integer mappedPort = localstackContainer.getMappedPort(TEST_PORT_S3);

        s3Service = new S3Service(createS3Client(mappedPort));
        s3Service.createBucket(TESTBUCKET);
    }

    @AfterEach
    public void tearDown() {
        s3Service.deleteAllObjectsInBucket(TESTBUCKET);
    }

    @Test
    public void shouldOnlyDeleteFilesWithPrefix() throws Exception {
        // given
        s3Service.upload(TESTBUCKET, createTestfile("test", ".txt", "Hello World!"));
        s3Service.upload(TESTBUCKET, createTestfile("prefix", ".txt", "Hello World!"));

        // when
        s3Service.deleteAllObjectsWithPrefixInBucket(TESTBUCKET, "prefix");

        // then
        final List<String> allFiles = s3Service.listAllFiles(TESTBUCKET);
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
        final List<String> allFiles = s3Service.listAllFiles(TESTBUCKET);
        assertThat(allFiles, hasSize(0));
    }

    private File createTestfile(final String prefix, final String suffix, final String content) throws Exception  {
        final File tempFile = createTempFile(prefix, suffix).toFile();
        try (final FileWriter writer = new FileWriter(tempFile)) {
            writer.append(content);
            writer.flush();
        }
        return tempFile;
    }

}
