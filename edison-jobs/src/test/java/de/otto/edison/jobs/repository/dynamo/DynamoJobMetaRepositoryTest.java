package de.otto.edison.jobs.repository.dynamo;

import de.otto.edison.jobs.domain.JobMeta;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.ImmutableMap;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static de.otto.edison.jobs.repository.dynamo.DynamoJobMetaRepository.JOB_TYPE_KEY;
import static de.otto.edison.jobs.repository.dynamo.DynamoJobMetaRepository.KEY_DISABLED;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Testcontainers
public class DynamoJobMetaRepositoryTest {

    @Container
    private static GenericContainer<?> dynamodb = createTestContainer()
            .withExposedPorts(8000);

    private static final String TABLE_NAME = "jobMeta";

    private static DynamoJobMetaRepository dynamoJobMetaRepository;

    public static GenericContainer<?> createTestContainer() {
        return new GenericContainer<>("amazon/dynamodb-local:latest");
    }

    @BeforeEach
    public void before() {
        createJobInfoTable();
        dynamoJobMetaRepository = new DynamoJobMetaRepository(getDynamoDbClient(), TABLE_NAME);
    }

    private static DynamoDbClient getDynamoDbClient() {
        String endpointUri = "http://" + dynamodb.getContainerIpAddress() + ":" +
                dynamodb.getMappedPort(8000);

        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpointUri))
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials.create("acc", "sec"))).build();
    }

    @AfterEach
    void tearDown() {
        deleteJobInfoTable();
    }

    private void createJobInfoTable() {
        getDynamoDbClient().createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(
                        KeySchemaElement.builder().attributeName(JOB_TYPE_KEY).keyType(KeyType.HASH).build()
                )
                .attributeDefinitions(
                        AttributeDefinition.builder().attributeName(JOB_TYPE_KEY).attributeType(ScalarAttributeType.S).build()
                )
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(1L)
                        .writeCapacityUnits(1L)
                        .build())
                .build());
    }

    private void deleteJobInfoTable() {
        getDynamoDbClient().deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME).build());
    }

    @Test
    void shouldSetRunningJob() {
        //when
        boolean notRunning = dynamoJobMetaRepository.setRunningJob("myJobType", "myJobId");


        //given
        String jobId = dynamoJobMetaRepository.getRunningJob("myJobType");
        assertThat(jobId, is("myJobId"));
        assertThat(notRunning, is(true));
    }

    @Test
    public void shouldNotSetRunningWhenIsAlreadyRunning() {
        //given
        dynamoJobMetaRepository.setRunningJob("myJobType", "otherJobId");

        //when
        boolean notRunning = dynamoJobMetaRepository.setRunningJob("myJobType", "myJobId");

        //given
        String jobId = dynamoJobMetaRepository.getRunningJob("myJobType");
        assertThat(jobId, is("otherJobId"));
        assertThat(notRunning, is(false));
    }

    @Test
    void shouldSetAndGetValue() {
        //given

        //when
        dynamoJobMetaRepository.setValue("myJobType", "someKey", "someValue");

        //given
        String value = dynamoJobMetaRepository.getValue("myJobType", "someKey");
        assertThat(value, is("someValue"));
    }

    @Test
    public void shouldReturnPreviousValueOnSetValue() {
        //given
        dynamoJobMetaRepository.setValue("myJobType", "someKey", "someOldValue");

        //when
        String previousValue = dynamoJobMetaRepository.setValue("myJobType", "someKey", "someNewValue");

        //given
        assertThat(previousValue, is("someOldValue"));
    }

    @Test
    public void shouldRemoveKeyWhenValueIsNull() {
        //given
        dynamoJobMetaRepository.setValue("myJobType", "someKey", "someOldValue");

        //when
        String previousValue = dynamoJobMetaRepository.setValue("myJobType", "someKey", null);

        //given
        assertThat(previousValue, is("someOldValue"));
        String value = dynamoJobMetaRepository.getValue("myJobType", "someKey");
        assertThat(value, nullValue());
    }

    @Test
    public void createValueShouldAddKeyIfNotExists() {

        //when
        boolean valueCreated = dynamoJobMetaRepository.createValue("myJobType", "someKey", "someValue");

        //then
        assertThat(valueCreated, is(true));
        String value = dynamoJobMetaRepository.getValue("myJobType", "someKey");
        assertThat(value, is("someValue"));
    }

    @Test
    public void createValueShouldNotAddKeyIfAlreadyExists() {
        //given
        dynamoJobMetaRepository.createValue("myJobType", "someKey", "someExistingValue");

        //when
        boolean valueCreated = dynamoJobMetaRepository.createValue("myJobType", "someKey", "someOtherValue");

        //then
        assertThat(valueCreated, is(false));
        String value = dynamoJobMetaRepository.getValue("myJobType", "someKey");
        assertThat(value, is("someExistingValue"));
    }

    @Test
    public void shouldSetDisabledWithComment() {
        //when
        dynamoJobMetaRepository.disable("someJobType", "someComment");

        //then
        String value = dynamoJobMetaRepository.getValue("someJobType", KEY_DISABLED);
        assertThat(value, is("someComment"));
    }

    @Test
    public void shouldSetDisabledWithoutComment() {
        //when
        dynamoJobMetaRepository = new DynamoJobMetaRepository(getDynamoDbClient(), TABLE_NAME);
        dynamoJobMetaRepository.disable("someJobType", null);

        //then
        String value = dynamoJobMetaRepository.getValue("someJobType", KEY_DISABLED);
        assertThat(value, nullValue());
    }

    @Test
    public void shouldSetEnabled() {
        //given
        dynamoJobMetaRepository.disable("someJobType", "disabled");

        //when
        dynamoJobMetaRepository.enable("someJobType");

        //then
        String value = dynamoJobMetaRepository.getValue("someJobType", KEY_DISABLED);
        assertThat(value, nullValue());
    }

    @Test
    public void shouldClearRunningJob() {
        //given
        dynamoJobMetaRepository.setRunningJob("someJobType", "someJobId");

        //when
        dynamoJobMetaRepository.clearRunningJob("someJobType");

        //then
        String jobId = dynamoJobMetaRepository.getRunningJob("someJobType");
        assertThat(jobId, nullValue());
    }

    @Test
    public void shouldFindAllJobTypes() {
        //given
        dynamoJobMetaRepository.setRunningJob("someJobType", "someJobId1");
        dynamoJobMetaRepository.setRunningJob("someOtherJobType", "someJobId2");
        dynamoJobMetaRepository.enable("oneMoreJobType");

        //when
        Set<String> allJobTypes = dynamoJobMetaRepository.findAllJobTypes();

        //then
        assertThat(allJobTypes, contains("someJobType", "someOtherJobType", "oneMoreJobType"));
    }

    @Test
    public void shouldReturnJobMeta() {
        //given
        dynamoJobMetaRepository.setRunningJob("someJobType", "someJobId");
        dynamoJobMetaRepository.disable("someJobType", "because");
        dynamoJobMetaRepository.setValue("someJobType", "foo", "bar");

        //when
        JobMeta jobMeta = dynamoJobMetaRepository.getJobMeta("someJobType");

        //then
        assertThat(jobMeta.getJobType(), is("someJobType"));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("because"));
        assertThat(jobMeta.isRunning(), is(true));
        assertThat(jobMeta.getAll(), is(ImmutableMap.of("foo", "bar")));
    }

    @Test
    public void shouldSetDisabledCommentToEmptyStringWhenEnabled() {
        //given
        dynamoJobMetaRepository.setRunningJob("someJobType", "someJobId");
        dynamoJobMetaRepository.setValue("someJobType", "foo", "bar");

        //when
        JobMeta jobMeta = dynamoJobMetaRepository.getJobMeta("someJobType");

        //then
        assertThat(jobMeta.getJobType(), is("someJobType"));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
    }

    @Test
    public void shouldReturnEmptyJobMetaWhenJobTypeDoesNotExist() {
        //when
        JobMeta jobMeta = dynamoJobMetaRepository.getJobMeta("someJobType");

        //then
        assertThat(jobMeta.getJobType(), is("someJobType"));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getAll(), is(Collections.emptyMap()));
    }

    @Test
    void shouldDeleteAll() {
        //given
        // 25 is the max delete batch size
        for (int i = 0; i < 26; i++) {
            String jobType = "someJobType" + i;
            String key = "someKey" + i;
            String value = "someValue" + i;
            dynamoJobMetaRepository.createValue(jobType, key, value);
        }

        //when
        dynamoJobMetaRepository.deleteAll();

        //then
        assertThat(dynamoJobMetaRepository.findAllJobTypes(), is(emptySet()));
    }
}