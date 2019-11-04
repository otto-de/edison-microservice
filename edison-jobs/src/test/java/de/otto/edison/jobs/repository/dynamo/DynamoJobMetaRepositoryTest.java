package de.otto.edison.jobs.repository.dynamo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.jobs.domain.JobMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    private static Stream<Arguments> metaInformation() {
        return Stream.of(
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "running", "false", "disabled", "false", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), false, false, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "running", "true", "disabled", "true", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), true, true, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "disabled", "true", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), false, true, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "running", "false", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), false, false, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "disabledComment", "someDisableComment", "lastEntryId", "someLastEntryId"), false, false, "someJobType", "someDisableComment"),
                Arguments.of(ImmutableMap.of("jobType", "someJobType", "running", "false", "disabled", "false", "lastEntryId", "someLastEntryId"), false, false, "someJobType", "")
        );
    }
}