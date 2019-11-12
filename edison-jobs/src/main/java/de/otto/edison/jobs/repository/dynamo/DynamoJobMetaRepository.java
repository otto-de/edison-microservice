package de.otto.edison.jobs.repository.dynamo;

import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;

public class DynamoJobMetaRepository implements JobMetaRepository {

    private static final String KEY_DISABLED = "_e_disabled";
    private static final String KEY_RUNNING = "_e_running";
    private static final String JOB_META_TABLE_NAME = "FT6_DynamoDB_JobMeta";
    private final DynamoDbClient dynamoDbClient;

    public DynamoJobMetaRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
        try {
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName(JOB_META_TABLE_NAME)
                    .build());
        } catch (ResourceNotFoundException e) {
            createTable();
        }
    }

    @Override
    public JobMeta getJobMeta(String jobType) {
        ImmutableMap<String, AttributeValue> key = ImmutableMap.of("jobType", toAttributeValue(jobType));
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName(JOB_META_TABLE_NAME)
                .key(key)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);
        Map<String, AttributeValue> responseItem = response.item();
        if (!responseItem.isEmpty()) {

            final boolean isRunning = responseItem.containsKey(KEY_RUNNING);
            final boolean isDisabled = responseItem.containsKey(KEY_DISABLED);
            final AttributeValue attributeValueComment = responseItem.get(KEY_DISABLED);
            String comment = null;
            if (attributeValueComment != null) {
                comment = attributeValueComment.s();
            }

            Map<String, String> metaMap = responseItem.entrySet().stream()
                    .filter(e -> !e.getKey().startsWith("_e_"))
                    .filter(e -> !e.getKey().equals("jobType"))
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().s()));
            return new JobMeta(jobType, isRunning, isDisabled, comment, metaMap);
        } else {
            return new JobMeta(jobType, false, false, "", emptyMap());
        }
    }

    @Override
    public boolean createValue(String jobType, String key, String value) {
        if (getValue(jobType, key) == null) {
            setValue(jobType, key, value);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean setRunningJob(String jobType, String jobId) {
        return createValue(jobType, KEY_RUNNING, jobId);
    }

    @Override
    public String getRunningJob(String jobType) {
        return getValue(jobType, KEY_RUNNING);
    }

    @Override
    public void clearRunningJob(String jobType) {
        setValue(jobType, KEY_RUNNING, null);
    }

    @Override
    public void disable(String jobType, String comment) {
        setValue(jobType, KEY_DISABLED, comment != null ? comment : "");

    }

    @Override
    public void enable(String jobType) {
        setValue(jobType, KEY_DISABLED, null);
    }

    @Override
    public String setValue(String jobType, String key, String value) {
        putIfAbsent(jobType);
        return putValue(jobType, key, value);
    }

    private String putValue(String jobType, String key, String value) {
        ImmutableMap<String, AttributeValue> itemRequestKey = ImmutableMap.of("jobType", toAttributeValue(jobType));
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName(JOB_META_TABLE_NAME)
                .key(itemRequestKey)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);
        Map<String, AttributeValue> newEntry = new HashMap<>();

        AttributeValue previousAttributeValue = response.item().get(key);
        String previous = null;
        if (previousAttributeValue != null) {
            previous = previousAttributeValue.s();
        }

        newEntry.putAll(response.item());
        if (value == null) {
            newEntry.remove(key);
        } else {
            newEntry.put(key, toAttributeValue(value));
        }

        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(JOB_META_TABLE_NAME)
                .item(newEntry)
                .build();
        dynamoDbClient.putItem(putItemRequest);

        return previous;
    }

    private AttributeValue toAttributeValue(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private void putIfAbsent(String jobType) {
        ImmutableMap<String, AttributeValue> itemRequestKey = ImmutableMap.of("jobType", toAttributeValue(jobType));
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName(JOB_META_TABLE_NAME)
                .key(itemRequestKey)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);
        if (response.item().isEmpty()) {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("jobType", toAttributeValue(jobType));
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(JOB_META_TABLE_NAME)
                    .item(item)
                    .build();
            dynamoDbClient.putItem(putItemRequest);
        }
    }

    @Override
    public String getValue(String jobType, String key) {
        ImmutableMap<String, AttributeValue> getItemRequestKey = ImmutableMap.of("jobType", toAttributeValue(jobType));
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName(JOB_META_TABLE_NAME)
                .key(getItemRequestKey)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);
        AttributeValue value = response.item().get(key);
        if (value != null) {
            return value.s();
        } else {
            return null;
        }
    }

    @Override
    public Set<String> findAllJobTypes() {
        ScanRequest scanRequest = ScanRequest.builder()
                .tableName(JOB_META_TABLE_NAME)
                .attributesToGet("jobType").build();
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
        Set<String> jobTypes = scanResponse.items().stream().map(m -> m.get("jobType").s()).collect(Collectors.toSet());
        return jobTypes;
    }

    @Override
    public void deleteAll() {
        deleteTable();
        createTable();
    }

    private void createTable() {
        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(JOB_META_TABLE_NAME)
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

    private void deleteTable() {
        DeleteTableRequest deleteTableRequest = DeleteTableRequest.builder()
                .tableName(JOB_META_TABLE_NAME).build();
        dynamoDbClient.deleteTable(deleteTableRequest);
    }
}
