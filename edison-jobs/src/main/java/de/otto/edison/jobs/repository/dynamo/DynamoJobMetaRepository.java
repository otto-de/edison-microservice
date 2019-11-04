package de.otto.edison.jobs.repository.dynamo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
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
        final boolean isRunning = meta.containsKey("running");
        final boolean isDisabled = meta.containsKey("disabled");
        final String comment = meta.get("disabledComment");

        return new JobMeta(jobType, isRunning, isDisabled, comment, meta);
    }

    @Override
    public boolean createValue(String jobType, String key, String value) {
        ImmutableMap<String, AttributeValue> itemRequestKey = ImmutableMap.of("jobType", AttributeValue.builder().s(jobType).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName("jobMeta")
                .key(itemRequestKey)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);

        HashMap<String, AttributeValue> item = new HashMap<>(response.item());
        item.put(key, AttributeValue.builder().s(value).build());
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName("jobMeta")
                .item(item)
                .build();

        dynamoDbClient.putItem(putItemRequest);

        return true;
    }

    @Override
    public boolean setRunningJob(String jobType, String jobId) {
        return createValue(jobType, "running", jobId);
    }

    @Override
    public String getRunningJob(String jobType) {
        return getValue(jobType, "running");
    }

    @Override
    public void clearRunningJob(String jobType) {
        setValue(jobType, "running", null);
    }

    @Override
    public void disable(String jobType, String comment) {

    }

    @Override
    public void enable(String jobType) {

    }

    @Override
    public String setValue(String jobType, String key, String value) {
        ImmutableMap<String, AttributeValue> itemRequestKey = ImmutableMap.of("jobType", AttributeValue.builder().s(jobType).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName("jobMeta")
                .key(itemRequestKey)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);
        String previous = null;
        Map<String, AttributeValue> responseItem = response.item();
        if (responseItem != null) {
            previous = responseItem.get(key) != null ? responseItem.get(key).s() : null;

            HashMap<String, AttributeValue> item = new HashMap<>(responseItem);
            item.put(key, AttributeValue.builder().s(value).build());

            if (value == null) {
                item.remove(key);
            }

            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName("jobMeta")
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putItemRequest);
        }

        return previous;
    }

    @Override
    public String getValue(String jobType, String key) {
        ImmutableMap<String, AttributeValue> getItemRequestKey = ImmutableMap.of("jobType", AttributeValue.builder().s(jobType).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName("jobMeta")
                .key(getItemRequestKey)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);
        return response.item().get(key).s();
    }

    @Override
    public Set<String> findAllJobTypes() {
        return null;
    }

    @Override
    public void deleteAll() {

    }
}
