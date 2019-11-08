package de.otto.edison.jobs.repository.dynamo;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.JobRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class DynamoJobRepository implements JobRepository {

    public static final String JOBS_TABLENAME = "jobs";
    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;

    public DynamoJobRepository(DynamoDbClient dynamoDbClient, ObjectMapper objectMapper) {
        this.dynamoDbClient = dynamoDbClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<JobInfo> findOne(String jobId) {
        Map<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(JobStructure.ID.key(), toStringAttributeValue(jobId));

        GetItemRequest jobInfoRequest = GetItemRequest.builder()
                .tableName(JOBS_TABLENAME)
                .key(keyMap)
                .build();
        final GetItemResponse jobInfoResponse = dynamoDbClient.getItem(jobInfoRequest);
        if (jobInfoResponse.item().isEmpty()) {
            return Optional.empty();
        }
        final JobInfo jobInfo = decode(jobInfoResponse.item());
        return Optional.of(jobInfo);

    }


    @Override
    public List<JobInfo> findLatest(int maxCount) {
        return null;
    }

    @Override
    public List<JobInfo> findLatestJobsDistinct() {
        return null;
    }

    @Override
    public List<JobInfo> findLatestBy(String type, int maxCount) {
        return null;
    }

    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset) {
        return null;
    }

    @Override
    public List<JobInfo> findAll() {
        ScanRequest findAll = ScanRequest.builder()
                .tableName(JOBS_TABLENAME)
                .build();
        final ScanResponse scan = dynamoDbClient.scan(findAll);
        return scan.items().stream().map(this::decode).collect(Collectors.toList());
    }

    @Override
    public List<JobInfo> findAllJobInfoWithoutMessages() {
        return null;
    }

    @Override
    public List<JobInfo> findByType(String jobType) {
        return null;
    }

    @Override
    public JobInfo createOrUpdate(JobInfo job) {

        Map<String, AttributeValue> jobAsItem = encode(job);
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(JOBS_TABLENAME)
                .item(jobAsItem)
                .build();
        dynamoDbClient.putItem(putItemRequest);

        return job;

    }

    private Map<String, AttributeValue> encode(JobInfo jobInfo) {
        Map<String, AttributeValue> jobAsItem = new HashMap<>();
        jobAsItem.put(JobStructure.ID.key(), toStringAttributeValue(jobInfo.getJobId()));
        jobAsItem.put(JobStructure.HOSTNAME.key(), toStringAttributeValue(jobInfo.getHostname()));
        jobAsItem.put(JobStructure.JOB_TYPE.key(), toStringAttributeValue(jobInfo.getJobType()));
        jobAsItem.put(JobStructure.STARTED.key(), toStringAttributeValue(jobInfo.getStarted()));
        jobAsItem.put(JobStructure.STATUS.key(), toStringAttributeValue(jobInfo.getStatus().name()));
        jobInfo.getStopped().ifPresent(offsetDateTime -> jobAsItem.put(JobStructure.STOPPED.key(), toStringAttributeValue(offsetDateTime)));
        if (null != jobInfo.getLastUpdated()) {
            jobAsItem.put(JobStructure.LAST_UPDATED.key(), toStringAttributeValue(jobInfo.getLastUpdated()));
        }
        jobAsItem.put(JobStructure.MESSAGES.key(), messagesToAttributeValueList(jobInfo.getMessages()));


        return jobAsItem;
    }

    private JobInfo decode(Map<String, AttributeValue> item) {
        final JobInfo.Builder jobInfo = JobInfo.builder()
                .setJobId(item.get(JobStructure.ID.key()).s())
                .setHostname(item.get(JobStructure.HOSTNAME.key()).s())
                .setJobType(item.get(JobStructure.JOB_TYPE.key()).s())
                .setStarted(OffsetDateTime.parse(item.get(JobStructure.STARTED.key()).s()))
                .setStatus(JobInfo.JobStatus.valueOf(item.get(JobStructure.STATUS.key()).s()))
                .setMessages(itemToJobMessages(item));
        if (item.containsKey(JobStructure.STOPPED.key())) {
            jobInfo.setStopped(OffsetDateTime.parse(item.get(JobStructure.STOPPED.key()).s()));
        }

        if (item.containsKey(JobStructure.LAST_UPDATED.key())) {
            jobInfo.setLastUpdated(OffsetDateTime.parse(item.get(JobStructure.LAST_UPDATED.key()).s()));
        }
        return jobInfo.build();
    }

    private List<JobMessage> itemToJobMessages(Map<String, AttributeValue> item) {
        if (!item.containsKey(JobStructure.MESSAGES.key())) {
            return emptyList();
        }

        final AttributeValue attributeValue = item.get(JobStructure.MESSAGES.key());
        return attributeValue.l().stream().map(this::attributeValueToMessage).collect(Collectors.toList());
    }

    private JobMessage attributeValueToMessage(AttributeValue attributeValue) {
        final Map<String, AttributeValue> messageMap = attributeValue.m();
        final Level level = Level.ofKey(messageMap.get(JobStructure.MSG_LEVEL.key()).s());
        final String text = messageMap.get(JobStructure.MSG_TEXT.key()).s();
        final OffsetDateTime timestamp = OffsetDateTime.parse(messageMap.get(JobStructure.MSG_TS.key()).s());
        return JobMessage.jobMessage(level, text, timestamp);
    }

    @Override
    public void removeIfStopped(String jobId) {

        Map<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(JobStructure.ID.key(), toStringAttributeValue(jobId));
        DeleteItemRequest deleteJobRequest = DeleteItemRequest.builder()
                .tableName(JOBS_TABLENAME)
                .key(keyMap)
                .build();
        dynamoDbClient.deleteItem(deleteJobRequest);
    }

    @Override
    public JobInfo.JobStatus findStatus(String jobId) {
        return null;
    }

    @Override
    public void appendMessage(String jobId, JobMessage jobMessage) {

    }

    @Override
    public void setJobStatus(String jobId, JobInfo.JobStatus jobStatus) {

    }

    @Override
    public void setLastUpdate(String jobId, OffsetDateTime lastUpdate) {

    }

    @Override
    public long size() {
        Map<String, AttributeValue> lastKeyEvaluated = null;
        long count = 0;
        do {
            ScanRequest counterQuery = ScanRequest.builder()
                    .tableName(JOBS_TABLENAME)
                    .select(Select.COUNT)
                    .limit(2)
                    .exclusiveStartKey(lastKeyEvaluated)
                    .build();

            final ScanResponse countResponse = dynamoDbClient.scan(counterQuery);
            lastKeyEvaluated = countResponse.lastEvaluatedKey();
            count = count + countResponse.count();
        } while (lastKeyEvaluated != null && lastKeyEvaluated.size() > 0);

        return count;
    }

    @Override
    public void deleteAll() {

    }

    private AttributeValue toStringAttributeValue(OffsetDateTime value) {
        return toStringAttributeValue(value.toString());
    }

    private AttributeValue toStringAttributeValue(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private AttributeValue toMapAttributeValue(JobMessage jobMessage) {
        Map<String, AttributeValue> message = new HashMap<>();
        message.put(JobStructure.MSG_LEVEL.key(), toStringAttributeValue(jobMessage.getLevel().getKey()));
        message.put(JobStructure.MSG_TEXT.key(), toStringAttributeValue(jobMessage.getMessage()));
        message.put(JobStructure.MSG_TS.key(), toStringAttributeValue(jobMessage.getTimestamp()));
        return AttributeValue.builder()
                .m(message)
                .build();
    }

    private AttributeValue messagesToAttributeValueList(List<JobMessage> jobeMessages) {
        final List<AttributeValue> messageAttributes = jobeMessages.stream().map(this::toMapAttributeValue).collect(Collectors.toList());
        return toAttributeValueList(messageAttributes);
    }

    private AttributeValue toAttributeValueList(List<AttributeValue> values) {
        return AttributeValue.builder().l(values).build();
    }

    public void setupSchema() {
        dynamoDbClient.createTable(CreateTableRequest.builder()
                .tableName(JOBS_TABLENAME)
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName(JobStructure.ID.key())
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName(JobStructure.ID.key())
                        .keyType(KeyType.HASH)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(10L)
                        .writeCapacityUnits(10L)
                        .build())
                .build());
    }

    public void deleteTable() {
        DeleteTableRequest deleteTableRequest = DeleteTableRequest.builder()
                .tableName(JOBS_TABLENAME).build();
        dynamoDbClient.deleteTable(deleteTableRequest);
    }

}
