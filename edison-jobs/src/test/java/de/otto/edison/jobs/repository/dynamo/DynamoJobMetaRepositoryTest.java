package de.otto.edison.jobs.repository.dynamo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.jobs.domain.JobMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.utils.ImmutableMap;

import javax.management.Attribute;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DynamoJobMetaRepositoryTest {

    private DynamoJobMetaRepository testee;

    private DynamoDbClient dynamoDbClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dynamoDbClient = mock(DynamoDbClient.class);
        testee = new DynamoJobMetaRepository(dynamoDbClient, objectMapper);
    }

    @ParameterizedTest
    @MethodSource("metaInformation")
    public void shouldGetJobMetaFromDbWithIncompleteInformation(final ImmutableMap<String, String> givenMeta,
                                                                final boolean isRunning,
                                                                final boolean isDisabled,
                                                                final String jobType,
                                                                final String disableComment) throws JsonProcessingException {
        //given
        String jsonifiedMeta = objectMapper.writeValueAsString(givenMeta);
        GetItemResponse getItemResponse = GetItemResponse.builder()
                .item(ImmutableMap.of("jobMeta", AttributeValue.builder().s(jsonifiedMeta).build()))
                .build();

        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(getItemResponse);

        //when
        JobMeta jobMeta = testee.getJobMeta("someJobType");


        //then
        assertThat(jobMeta.isRunning(), is(isRunning));
        assertThat(jobMeta.isDisabled(), is(isDisabled));
        assertThat(jobMeta.getJobType(), is(jobType));
        assertThat(jobMeta.getDisabledComment(), is(disableComment));
        assertThat(jobMeta.getAll(), is(givenMeta));
    }

    @Test
    void shouldCreatePutItemRequest() {
        //given
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(GetItemResponse.builder().build());

        //when
        testee.createValue("someNonExistingJobType", "foo", "bar");

        //then
        verify(dynamoDbClient).putItem(PutItemRequest.builder()
                .tableName("jobMeta")
                .item(ImmutableMap.of("foo", AttributeValue.builder().s("bar").build()))
                .build());
    }

    @Test
    void shouldGetValue() {
        //given
        GetItemRequest getItemRequest = GetItemRequest
                .builder()
                .tableName("jobMeta")
                .key(ImmutableMap.of("jobType",
                        AttributeValue.builder()
                                .s("someJobType").build()))
                .build();
        when(dynamoDbClient.getItem(eq(getItemRequest)))
                .thenReturn(GetItemResponse.builder().item(ImmutableMap.of("foo", AttributeValue.builder().s("bar").build())).build());

        //when
        String value = testee.getValue("someJobType", "foo");

        //then
        assertThat(value, is("bar"));
    }

    @Test
    void shouldReturnRunningStateOfJob() {
        //given
        GetItemRequest getItemRequest = GetItemRequest
                .builder()
                .tableName("jobMeta")
                .key(ImmutableMap.of("jobType",
                        AttributeValue.builder()
                                .s("someJobType").build()))
                .build();
        when(dynamoDbClient.getItem(eq(getItemRequest)))
                .thenReturn(GetItemResponse.builder().item(ImmutableMap.of("running", AttributeValue.builder().s("someJobId").build())).build());

        //when
        String isRunning = testee.getRunningJob("someJobType");

        //then
        assertThat(isRunning, is("someJobId"));

    }

    @Test
    void shouldSetRunningStateOfJob() {
        //given
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(GetItemResponse.builder().build());

        //when
        testee.setRunningJob("someJobType", "someJobId");

        //then
        verify(dynamoDbClient).putItem(PutItemRequest.builder()
                .tableName("jobMeta")
                .item(ImmutableMap.of("running", AttributeValue.builder().s("someJobId").build()))
                .build());
    }

    @Test
    void shouldClearRunningJob() {
        //given
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(GetItemResponse.builder().build());

        //when
        testee.clearRunningJob("someJobType");

        //then
        verify(dynamoDbClient).putItem(PutItemRequest.builder()
                .tableName("jobMeta")
                .build());
    }

    @Test
    void shouldUpdateValue() {
        //given
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(
                GetItemResponse.builder()
                        .item(ImmutableMap.of("foo", AttributeValue.builder().s("qux").build()))
                        .build());

        //when
        testee.setValue("someJobType", "foo", "bar");

        //then
        verify(dynamoDbClient).putItem(PutItemRequest.builder()
                .tableName("jobMeta")
                .item(ImmutableMap.of("foo", AttributeValue.builder().s("bar").build()))
                .build());
    }

    @Test
    void shouldUnsetValue() {
        //given
        when(dynamoDbClient.getItem(any(GetItemRequest.class))).thenReturn(
                GetItemResponse.builder()
                        .item(ImmutableMap.of("someKey1", AttributeValue.builder().s("someValue1").build(),
                                "someKey2", AttributeValue.builder().s("someValue2").build()))
                        .build());

        //when
        testee.setValue("someJobType", "someKey1", null);

        //then
        verify(dynamoDbClient).putItem(PutItemRequest.builder()
                .tableName("jobMeta")
                .item(ImmutableMap.of("someKey2", AttributeValue.builder().s("someValue2").build()))
                .build());
    }

    private static Stream<Arguments> metaInformation() {
        return Stream.of(
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "running", "someJobId1", "disabled", "disabled", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), true, true, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "running", "someJobId2", "disabled", "disabled", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), true, true, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "disabled", "disabled", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), false, true, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "running", "someJobId3", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), true, false, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), false, false, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "running", "someJobId4", "disabled", "disabled", "lastEntryId", "someLastEntryId"), true, true, "someJobType", "")
        );
    }
}