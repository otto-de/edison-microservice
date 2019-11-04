package de.otto.edison.jobs.repository.dynamo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DynamoJobMetaRepository implements JobMetaRepository {

    private final MapType mapType;
    private final DynamoDbClient dynamoDbClient;

    private final ObjectMapper objectMapper;

    public DynamoJobMetaRepository(DynamoDbClient dynamoDbClient, ObjectMapper objectMapper) {
        this.dynamoDbClient = dynamoDbClient;
        this.objectMapper = objectMapper;
        this.mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
    }

    @Override
    public JobMeta getJobMeta(String jobType) {
        ImmutableMap<String, AttributeValue> key = ImmutableMap.of("jobType", AttributeValue.builder().s(jobType).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName("jobMeta")
                .key(key)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);
        String metaRaw = response.item().get("jobMeta").s();

        Map<String, String> meta = null;
        try {
            meta = objectMapper.readValue(metaRaw, mapType);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        final boolean isRunning = meta.containsKey("running") && Boolean.parseBoolean(meta.get("running"));
        final boolean isDisabled = meta.containsKey("disabled") && Boolean.parseBoolean(meta.get("disabled"));
        final String comment = meta.get("disabledComment");

        return new JobMeta(jobType, isRunning, isDisabled, comment, meta);
    }

    @Override
    public boolean createValue(String jobType, String key, String value) {
        return false;
    }

    @Override
    public boolean setRunningJob(String jobType, String jobId) {
        return false;
    }

    @Override
    public String getRunningJob(String jobType) {
        return null;
    }

    @Override
    public void clearRunningJob(String jobType) {

    }

    @Override
    public void disable(String jobType, String comment) {

    }

    @Override
    public void enable(String jobType) {

    }

    @Override
    public String setValue(String jobType, String key, String value) {
        return null;
    }

    @Override
    public String getValue(String jobType, String key) {
        return null;
    }

    @Override
    public Set<String> findAllJobTypes() {
        return null;
    }

    @Override
    public void deleteAll() {

    }
}
