package de.otto.edison.jobs.repository.mongo;

import com.mongodb.client.MongoClients;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.dynamo.DynamoJobMetaRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobMetaRepository;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

@Testcontainers
public class JobMetaRepositoryTest {

    private static final String DYNAMO_JOB_META_TABLE_NAME = "FT6_DynamoDB_JobMeta";
    private static DynamoJobMetaRepository dynamoTestee = null;

    static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.5");

    @AfterAll
    public static void teardownMongo() {
        mongoDBContainer.stop();
    }

    @BeforeAll
    public static void initDbs() {
        mongoDBContainer.start();
        createDynamoTable();
        dynamoTestee = new DynamoJobMetaRepository(getDynamoDbClient(), DYNAMO_JOB_META_TABLE_NAME);
    }

    @Container
    private static GenericContainer<?> dynamodb = createTestContainer()
            .withExposedPorts(8000);

    @BeforeEach
    void setUp() {
        createDynamoTable();
        dynamoTestee = new DynamoJobMetaRepository(getDynamoDbClient(), DYNAMO_JOB_META_TABLE_NAME);
    }

    @AfterEach
    public void tearDown() {
        deleteDynamoTable();
    }

    private static GenericContainer<?> createTestContainer() {
        return new GenericContainer<>("amazon/dynamodb-local:latest");
    }

    private static void createDynamoTable() {
        try {
            getDynamoDbClient().describeTable(DescribeTableRequest.builder()
                    .tableName(DYNAMO_JOB_META_TABLE_NAME)
                    .build());
        } catch (ResourceNotFoundException e) {
            getDynamoDbClient().createTable(CreateTableRequest.builder()
                    .tableName(DYNAMO_JOB_META_TABLE_NAME)
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("jobType")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("jobType")
                            .keyType(KeyType.HASH)
                            .build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(10L)
                            .writeCapacityUnits(10L)
                            .build())
                    .build());
        }
    }

    private static void deleteDynamoTable() {
        DeleteTableRequest deleteTableRequest = DeleteTableRequest.builder()
                .tableName(DYNAMO_JOB_META_TABLE_NAME).build();
        getDynamoDbClient().deleteTable(deleteTableRequest);
    }

    private static Collection<JobMetaRepository> data() {
        return asList(
                new MongoJobMetaRepository(MongoClients.create(mongoDBContainer.getReplicaSetUrl()).getDatabase("jobmeta-" + UUID.randomUUID()),
                        "jobmeta",
                        new MongoProperties()),
                new InMemJobMetaRepository(),
                dynamoTestee
        );
    }

    private static DynamoDbClient getDynamoDbClient() {
        String endpointUri = "http://" + dynamodb.getHost() + ":" +
                dynamodb.getMappedPort(8000);
        return DynamoDbClient.builder()
                .endpointOverride(URI.create(endpointUri))
                .region(Region.EU_CENTRAL_1)
                .credentialsProvider(StaticCredentialsProvider
                        .create(AwsBasicCredentials.create("acc", "sec"))).build();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldStoreAndGetValue(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        
        //when
        testee.setValue("someJob", "someKey", "someValue");
        testee.setValue("someJob", "someOtherKey", "someDifferentValue");
        testee.setValue("someOtherJob", "someKey", "someOtherValue");

        //then
        assertThat(testee.getValue("someJob", "someKey"), is("someValue"));
        assertThat(testee.getValue("someJob", "someOtherKey"), is("someDifferentValue"));
        assertThat(testee.getValue("someOtherJob", "someKey"), is("someOtherValue"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldGetEmptyJobMeta(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        
        //when
        final JobMeta jobMeta = testee.getJobMeta("someJob");

        //then
        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldGetJobMetaForRunningJob(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.setRunningJob("someJob", "someId");
        
        //when
        final JobMeta jobMeta = testee.getJobMeta("someJob");

        //then
        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(true));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldGetJobMetaForDisabledJob(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.disable("someJob", "some comment");
        
        //when
        final JobMeta jobMeta = testee.getJobMeta("someJob");

        //then
        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("some comment"));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldGetJobMetaForDisabledJobWithProperties(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.disable("someJob", "some comment");
        testee.setValue("someJob", "someKey", "some value");
        
        //when
        final JobMeta jobMeta = testee.getJobMeta("someJob");

        //then
        assertThat(jobMeta.getAll(), is(singletonMap("someKey", "some value")));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("some comment"));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldEnableJob(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.setValue("someJob", "_e_disabled", "foo");

        //when
        testee.enable("someJob");

        //then
        assertThat(testee.getValue("someJob", "_e_disabled"), is(nullValue()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldDisableJob(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        
        //when
        testee.disable("someJob", "some comment");

        //then
        assertThat(testee.getValue("someJob", "_e_disabled"), is("some comment"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldSetRunningJob(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        
        //when
        testee.setRunningJob("someJob", "someId");

        //then
        assertThat(testee.getRunningJob("someJob"), is("someId"));
        assertThat(testee.getValue("someJob", "_e_running"), is("someId"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldDeleteAll(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.enable("foo");
        testee.enable("bar");

        //when
        testee.deleteAll();

        //then
        assertThat(testee.findAllJobTypes(), is(empty()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldClearRunningJob(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.setValue("someJob", "_e_running", "someId");

        //when
        testee.clearRunningJob("someJob");

        //then
        assertThat(testee.getRunningJob("someJob"), is(nullValue()));
        assertThat(testee.getValue("someJob", "_e_runnin"), is(nullValue()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldReturnNullForMissingKeys(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.setValue("someJob", "someKey", "someValue");

        //when/then
        assertThat(testee.getValue("someJob", "someMissingKey"), is(nullValue()));
        assertThat(testee.getValue("someMissingJob", "someMissingKey"), is(nullValue()));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldFindJobTypes(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.setValue("someJob", "someKey", "someValue");
        testee.setValue("someOtherJob", "someKey", "someOtherValue");

        //when
        final Set<String> allJobTypes = testee.findAllJobTypes();
        
        //then
        assertThat(allJobTypes, containsInAnyOrder("someJob", "someOtherJob"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldNotCreateIfExists(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.setValue("someJob", "someKey", "initialValue");

        //when
        final boolean value = testee.createValue("someJob", "someKey", "newValue");

        //then
        assertThat(value, is(false));
        assertThat(testee.getValue("someJob", "someKey"), is("initialValue"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldCreateIfNotExists(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        
        //when
        final boolean value = testee.createValue("someJob", "someKey", "someValue");

        //then
        assertThat(value, is(true));
        assertThat(testee.getValue("someJob", "someKey"), is("someValue"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldCreateTwoValuesWithoutException(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.createValue("someJob", "someKey", "someValue");

        //when
        final boolean value = testee.createValue("someJob", "someOtherKey", "someOtherValue");

        //then
        assertThat(value, is(true));
        assertThat(testee.getValue("someJob", "someKey"), is("someValue"));
        assertThat(testee.getValue("someJob", "someOtherKey"), is("someOtherValue"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldReturnFalseIfCreateWasCalledTwice(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.createValue("someJob", "someKey", "someInitialValue");

        //when
        final boolean value = testee.createValue("someJob", "someKey", "someValue");

        //then
        assertThat(value, is(false));
        assertThat(testee.getValue("someJob", "someKey"), is("someInitialValue"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldNotKillOldFieldsOnCreate(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.setValue("someJob", "someKey", "someInitialValue");

        //when
        final boolean value = testee.createValue("someJob", "someAtomicKey", "someValue");

        //then
        assertThat(value, is(true));
        assertThat(testee.getValue("someJob", "someKey"), is("someInitialValue"));
        assertThat(testee.getValue("someJob", "someAtomicKey"), is("someValue"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldUnsetKeyOnSetNullValue(final JobMetaRepository testee) {
        //given
        testee.deleteAll();
        testee.setValue("someJob", "someKey", "someValue");

        //when
        testee.setValue("someJob", "someKey", null);

        //then
        assertThat(testee.findAllJobTypes(), contains("someJob"));
        assertThat(testee.getValue("someJob", "someKey"), is(nullValue()));
    }
}
