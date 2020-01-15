package de.otto.edison.jobs.repository.dynamo;

import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class DynamoJobMetaRepository extends AbstractDynamoRepository implements JobMetaRepository {

    private static final String KEY_PREFIX = "_e_";
    static final String KEY_DISABLED = KEY_PREFIX + "disabled";
    private static final String KEY_RUNNING = KEY_PREFIX + "running";
    static final String JOB_TYPE_KEY = "jobType";
    private static final String ETAG_KEY = "etag";

    public DynamoJobMetaRepository(final DynamoDbClient dynamoDbClient, final String tableName) {
        super(dynamoDbClient, tableName);
        dynamoDbClient.describeTable(DescribeTableRequest.builder()
                .tableName(tableName)
                .build());
    }

    @Override
    public JobMeta getJobMeta(String jobType) {
        GetItemResponse response = getItem(jobType);
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
                    .filter(e -> !e.getKey().startsWith(KEY_PREFIX))
                    .filter(e -> !e.getKey().equals(JOB_TYPE_KEY))
                    .filter(e -> !e.getKey().equals(ETAG_KEY))
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
        setValue(jobType, KEY_DISABLED, comment);
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
        GetItemResponse getItemResponse = getItem(jobType);
        Map<String, AttributeValue> newEntry = new HashMap<>(getItemResponse.item());
        newEntry.put(key, toAttributeValue(value));
        newEntry.put(ETAG_KEY, AttributeValue.builder().s(UUID.randomUUID().toString()).build());

        final PutItemRequest.Builder putItemRequestBuilder = PutItemRequest.builder()
                .tableName(tableName)
                .item(newEntry);
        addEtagCondition(putItemRequestBuilder, getItemResponse);
        dynamoDbClient.putItem(putItemRequestBuilder.build());

        AttributeValue existingValueForKey = getItemResponse.item().get(key);
        return existingValueForKey == null ? null : existingValueForKey.s();
    }

    private void addEtagCondition(final PutItemRequest.Builder putItemRequestBuilder, final GetItemResponse getItemResponse) {
        final AttributeValue existingEtag = getItemResponse.item().get(ETAG_KEY);
        if (existingEtag != null) {
            Map<String, AttributeValue> valueMap = new HashMap<>();
            valueMap.put(":val", AttributeValue.builder().s(existingEtag.s()).build());
            putItemRequestBuilder.expressionAttributeValues(valueMap);
            putItemRequestBuilder.conditionExpression("contains(etag, :val)");
        }
    }

    private AttributeValue toAttributeValue(String value) {
        return value == null || value.isEmpty() ? AttributeValue.builder().nul(true).build() : AttributeValue.builder().s(value).build();
    }

    private void putIfAbsent(String jobType) {
        GetItemResponse response = getItem(jobType);
        if (response.item().isEmpty()) {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put(JOB_TYPE_KEY, toAttributeValue(jobType));
            PutItemRequest putItemRequest = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();
            dynamoDbClient.putItem(putItemRequest);
        }
    }

    @Override
    public String getValue(String jobType, String key) {
        GetItemResponse response = getItem(jobType);
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
                .tableName(tableName)
                .attributesToGet(JOB_TYPE_KEY).build();
        ScanResponse scanResponse = dynamoDbClient.scan(scanRequest);
        Set<String> jobTypes = scanResponse.items().stream().map(m -> m.get(JOB_TYPE_KEY).s()).collect(Collectors.toSet());
        return jobTypes;
    }

    @Override
    public void deleteAll() {
        final List<WriteRequest> deleteRequests = findAllJobTypes().stream()
                .map(jobId -> WriteRequest.builder()
                        .deleteRequest(
                                DeleteRequest.builder()
                                        .key(ImmutableMap.of(JOB_TYPE_KEY, AttributeValue.builder().s(jobId).build()))
                                        .build()
                        ).build()
                ).collect(toList());

        deleteEntriesPerBatch(deleteRequests);
    }

    private GetItemResponse getItem(String jobType) {
        ImmutableMap<String, AttributeValue> itemRequestKey = ImmutableMap.of(JOB_TYPE_KEY, toAttributeValue(jobType));
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(itemRequestKey)
                .build();
        return dynamoDbClient.getItem(itemRequest);
    }
}
