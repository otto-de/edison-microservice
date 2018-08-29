package de.otto.edison.aws.s3;

import de.otto.edison.aws.configuration.AwsConfiguration;
import de.otto.edison.aws.s3.configuration.S3Config;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;

import java.io.File;
import java.io.FileWriter;
import java.time.Duration;
import java.util.List;

import static de.otto.edison.aws.s3.S3TestHelper.createS3Client;
import static de.otto.edison.aws.s3.S3TestHelper.createTestContainer;
import static java.nio.file.Files.createTempFile;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AwsConfiguration.class, S3Config.class})
@TestPropertySource("classpath:application-test.properties")
public class S3ServiceIntegrationTest {

    private static final int TEST_PORT_S3 = 4572;
    private static final String TESTBUCKET = "testbucket";

    @ClassRule
    public final static GenericContainer localstackContainer = createTestContainer(TEST_PORT_S3);

    private S3Service s3Service;

    @Before
    public void setUp() {
        final Integer mappedPort = localstackContainer.getMappedPort(TEST_PORT_S3);

        s3Service = new S3Service(createS3Client(mappedPort));
        s3Service.createBucket(TESTBUCKET);
    }

    @After
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
