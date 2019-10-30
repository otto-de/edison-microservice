package de.otto.edison.jobs.repository.dynamo;

import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ItemResponse;
import software.amazon.awssdk.utils.ImmutableMap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class DynamoJobMetaRepository implements JobMetaRepository {

    private DynamoDbClient dynamoDbClient;

    public DynamoJobMetaRepository(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @Override
    public JobMeta getJobMeta(String jobType) {
        ImmutableMap<String, AttributeValue> key = ImmutableMap.of("jobType", AttributeValue.builder().s(jobType).build());
        GetItemRequest itemRequest = GetItemRequest.builder()
                .tableName("meta")
                .key(key)
                .build();
        GetItemResponse response = dynamoDbClient.getItem(itemRequest);
        Optional<JobMeta> meta = response.getValueForField("meta", JobMeta.class);
        return meta.orElse(null);
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
