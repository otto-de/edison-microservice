package de.otto.edison.jobs.repository.dynamo;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.JobRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.utils.ImmutableMap;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.otto.edison.jobs.repository.dynamo.JobStructure.*;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.*;

public class DynamoJobRepository extends AbstractDynamoRepository implements JobRepository {

    private static final String ETAG_KEY = "etag";
    private final int pageSize;

    public DynamoJobRepository(DynamoDbClient dynamoDbClient, final String tableName, int pageSize) {
        super(dynamoDbClient, tableName);
        this.pageSize = pageSize;
        dynamoDbClient.describeTable(DescribeTableRequest.builder()
                .tableName(tableName)
                .build());
    }

    @Override
    public Optional<JobInfo> findOne(String jobId) {
        return findOneItem(jobId).map(this::decode);
    }

    private Optional<Map<String, AttributeValue>> findOneItem(final String jobId) {
        Map<String, AttributeValue> keyMap = new HashMap<>();
        keyMap.put(ID.key(), toStringAttributeValue(jobId));
        GetItemRequest jobInfoRequest = GetItemRequest.builder()
                .tableName(tableName)
                .key(keyMap)
                .build();
        final GetItemResponse jobInfoResponse = dynamoDbClient.getItem(jobInfoRequest);
        if (jobInfoResponse.item().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(jobInfoResponse.item());
    }

    @Override
    public List<JobInfo> findLatest(int maxCount) {
        return findAll().stream()
                .sorted(Comparator.<JobInfo>comparingLong(jobInfo -> jobInfo.getStarted().toInstant().toEpochMilli()).reversed())
                .limit(maxCount)
                .collect(toList());
    }

    @Override
    public List<JobInfo> findLatestJobsDistinct() {
        return findAll().stream().collect(
                groupingBy(
                        JobInfo::getJobType,
                        maxBy(comparingLong(jobInfo -> jobInfo.getLastUpdated().toInstant().toEpochMilli()))
                ))
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    @Override
    public List<JobInfo> findLatestBy(String type, int maxCount) {
        return findByType(type).stream()
                .sorted(Comparator.<JobInfo>comparingLong(jobInfo ->
                        jobInfo.getStarted().toInstant().toEpochMilli()).reversed())
                .limit(maxCount)
                .collect(toList());
    }

    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(OffsetDateTime timeOffset) {
        Map<String, AttributeValue> lastKeyEvaluated = null;
        List<JobInfo> jobs = new ArrayList<>();
        Map<String, AttributeValue> expressionAttributeValues = ImmutableMap.of(
                ":val", AttributeValue.builder().n(String.valueOf(timeOffset.toInstant().toEpochMilli())).build()
        );
        do {
            final ScanRequest query = ScanRequest.builder()
                    .tableName(tableName)
                    .limit(pageSize)
                    .exclusiveStartKey(lastKeyEvaluated)
                    .expressionAttributeValues(expressionAttributeValues)
                    .filterExpression(LAST_UPDATED_EPOCH.key() + " < :val and attribute_not_exists(" + STOPPED.key() + ")")
                    .build();

            final ScanResponse response = dynamoDbClient.scan(query);
            lastKeyEvaluated = response.lastEvaluatedKey();
            List<JobInfo> newJobsFromThisPage = response.items().stream().map(this::decode).collect(toList());
            jobs.addAll(newJobsFromThisPage);
        } while (lastKeyEvaluated != null && lastKeyEvaluated.size() > 0);
        return jobs;
    }

    private List<JobInfo> findAll(final boolean withMessages) {
        Map<String, AttributeValue> lastKeyEvaluated = null;
        List<JobInfo> jobs = new ArrayList<>();
        do {
            final ScanRequest.Builder findAllRequestBuilder = ScanRequest.builder()
                    .tableName(tableName)
                    .limit(pageSize)
                    .exclusiveStartKey(lastKeyEvaluated);

            if (!withMessages) {
                String projectionExpressionBuilder = ID.key() +
                        ", " + STARTED.key() +
                        ", " + STOPPED.key() +
                        ", " + JOB_TYPE.key() +
                        ", #" + STATUS.key() +
                        ", " + HOSTNAME.key() +
                        ", " + LAST_UPDATED.key();
                findAllRequestBuilder.projectionExpression(projectionExpressionBuilder)
                        .expressionAttributeNames(ImmutableMap.of("#" + STATUS.key(), STATUS.key()));
            }
            final ScanResponse scan = dynamoDbClient.scan(findAllRequestBuilder.build());
            lastKeyEvaluated = scan.lastEvaluatedKey();
            List<JobInfo> newJobsFromThisPage = scan.items().stream().map(this::decode).collect(Collectors.toList());
            jobs.addAll(newJobsFromThisPage);
        } while (lastKeyEvaluated != null && lastKeyEvaluated.size() > 0);
        return jobs;
    }

    @Override
    public List<JobInfo> findAll() {
        return findAll(true);
    }

    @Override
    public List<JobInfo> findAllJobInfoWithoutMessages() {
        return findAll(false);
    }

    @Override
    public List<JobInfo> findByType(String jobType) {
        Map<String, AttributeValue> lastKeyEvaluated = null;
        List<JobInfo> jobs = new ArrayList<>();
        Map<String, AttributeValue> expressionAttributeValues = ImmutableMap.of(
                ":jobType", AttributeValue.builder().s(jobType).build()
        );
        do {
            final ScanRequest query = ScanRequest.builder()
                    .tableName(tableName)
                    .limit(pageSize)
                    .exclusiveStartKey(lastKeyEvaluated)
                    .expressionAttributeValues(expressionAttributeValues)
                    .filterExpression(JOB_TYPE.key() + " = :jobType")
                    .build();

            final ScanResponse response = dynamoDbClient.scan(query);
            lastKeyEvaluated = response.lastEvaluatedKey();
            List<JobInfo> newJobsFromThisPage = response.items().stream().map(this::decode).collect(toList());
            jobs.addAll(newJobsFromThisPage);
        } while (lastKeyEvaluated != null && lastKeyEvaluated.size() > 0);
        return jobs;
    }

    @Override
    public JobInfo createOrUpdate(final JobInfo job) {
        Map<String, AttributeValue> jobAsItem = encode(job);
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(tableName)
                .item(jobAsItem)
                .build();
        dynamoDbClient.putItem(putItemRequest);
        return job;
    }

    public JobInfo createOrUpdate(final JobInfo job, final AttributeValue etag) {
        Map<String, AttributeValue> jobAsItem = encode(job);
        final PutItemRequest.Builder putItemRequestBuilder = PutItemRequest.builder()
                .tableName(tableName)
                .item(jobAsItem);
        if (etag != null) {
            Map<String, AttributeValue> valueMap = new HashMap<>();
            valueMap.put(":val", AttributeValue.builder().s(etag.s()).build());
            putItemRequestBuilder.expressionAttributeValues(valueMap);
            putItemRequestBuilder.conditionExpression("contains(etag, :val)");
        }
        dynamoDbClient.putItem(putItemRequestBuilder.build());
        return job;
    }

    private Map<String, AttributeValue> encode(JobInfo jobInfo) {
        Map<String, AttributeValue> jobAsItem = new HashMap<>();
        jobAsItem.put(ID.key(), toStringAttributeValue(jobInfo.getJobId()));
        jobAsItem.put(HOSTNAME.key(), toStringAttributeValue(jobInfo.getHostname()));
        jobAsItem.put(JOB_TYPE.key(), toStringAttributeValue(jobInfo.getJobType()));
        jobAsItem.put(STARTED.key(), toStringAttributeValue(jobInfo.getStarted()));
        jobAsItem.put(STATUS.key(), toStringAttributeValue(jobInfo.getStatus().name()));
        jobInfo.getStopped().ifPresent(offsetDateTime -> jobAsItem.put(STOPPED.key(), toStringAttributeValue(offsetDateTime)));
        if (null != jobInfo.getLastUpdated()) {
            jobAsItem.put(LAST_UPDATED.key(), toStringAttributeValue(jobInfo.getLastUpdated()));
            jobAsItem.put(LAST_UPDATED_EPOCH.key(), toNumberAttributeValue(jobInfo.getLastUpdated().toInstant().toEpochMilli()));
        }
        jobAsItem.put(MESSAGES.key(), messagesToAttributeValueList(jobInfo.getMessages()));
        return jobAsItem;
    }

    private JobInfo decode(Map<String, AttributeValue> item) {
        final JobInfo.Builder jobInfo = JobInfo.builder()
                .setJobId(item.get(ID.key()).s())
                .setHostname(item.get(HOSTNAME.key()).s())
                .setJobType(item.get(JOB_TYPE.key()).s())
                .setStarted(OffsetDateTime.parse(item.get(STARTED.key()).s()))
                .setStatus(JobInfo.JobStatus.valueOf(item.get(STATUS.key()).s()))
                .setMessages(itemToJobMessages(item));
        if (item.containsKey(STOPPED.key())) {
            jobInfo.setStopped(OffsetDateTime.parse(item.get(STOPPED.key()).s()));
        }
        if (item.containsKey(LAST_UPDATED.key())) {
            jobInfo.setLastUpdated(OffsetDateTime.parse(item.get(LAST_UPDATED.key()).s()));
        }
        return jobInfo.build();
    }

    private List<JobMessage> itemToJobMessages(Map<String, AttributeValue> item) {
        if (!item.containsKey(MESSAGES.key())) {
            return emptyList();
        }
        final AttributeValue attributeValue = item.get(MESSAGES.key());
        return attributeValue.l().stream().map(this::attributeValueToMessage).collect(toList());
    }

    private JobMessage attributeValueToMessage(AttributeValue attributeValue) {
        final Map<String, AttributeValue> messageMap = attributeValue.m();
        final Level level = Level.ofKey(messageMap.get(MSG_LEVEL.key()).s());
        final String text = messageMap.get(MSG_TEXT.key()).s();
        final OffsetDateTime timestamp = OffsetDateTime.parse(messageMap.get(MSG_TS.key()).s());
        return JobMessage.jobMessage(level, text, timestamp);
    }

    @Override
    public void removeIfStopped(String jobId) {
        findOne(jobId).ifPresent(jobInfo -> {
            if (jobInfo.isStopped()) {
                Map<String, AttributeValue> keyMap = new HashMap<>();
                keyMap.put(ID.key(), toStringAttributeValue(jobId));
                DeleteItemRequest deleteJobRequest = DeleteItemRequest.builder()
                        .tableName(tableName)
                        .key(keyMap)
                        .build();
                dynamoDbClient.deleteItem(deleteJobRequest);
            }
        });
    }

    @Override
    public JobInfo.JobStatus findStatus(String jobId) {
        return findOne(jobId)
                .orElseThrow(RuntimeException::new)
                .getStatus();
    }

    @Override
    public void appendMessage(String jobId, JobMessage jobMessage) {
        final Map<String, AttributeValue> item = findOneItem(jobId).orElseThrow(RuntimeException::new);
        JobInfo jobInfo = decode(item);
        createOrUpdate(
                jobInfo.copy()
                        .addMessage(jobMessage)
                        .setLastUpdated(jobMessage.getTimestamp())
                        .build(),
                item.get(ETAG_KEY));
    }

    @Override
    public void setJobStatus(String jobId, JobInfo.JobStatus jobStatus) {
        final Map<String, AttributeValue> item = findOneItem(jobId).orElseThrow(RuntimeException::new);
        JobInfo jobInfo = decode(item);
        createOrUpdate(jobInfo.copy()
                        .setStatus(jobStatus)
                        .build(),
                item.get(ETAG_KEY));
    }

    @Override
    public void setLastUpdate(String jobId, OffsetDateTime lastUpdate) {
        final Map<String, AttributeValue> item = findOneItem(jobId).orElseThrow(RuntimeException::new);
        JobInfo jobInfo = decode(item);
        createOrUpdate(jobInfo.copy()
                        .setLastUpdated(lastUpdate)
                        .build(),
                item.get(ETAG_KEY));
    }

    @Override
    public long size() {
        Map<String, AttributeValue> lastKeyEvaluated = null;
        long count = 0;
        do {
            ScanRequest counterQuery = ScanRequest.builder()
                    .tableName(tableName)
                    .select(Select.COUNT)
                    .limit(pageSize)
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
        final List<WriteRequest> deleteRequests = findAll().stream()
                .map(JobInfo::getJobId)
                .map(jobId -> WriteRequest.builder()
                        .deleteRequest(
                                DeleteRequest.builder()
                                        .key(ImmutableMap.of(ID.key(), AttributeValue.builder().s(jobId).build()))
                                        .build()
                        ).build()
                ).collect(toList());

        deleteEntriesPerBatch(deleteRequests);
    }

    private AttributeValue toStringAttributeValue(OffsetDateTime value) {
        return toStringAttributeValue(value.toString());
    }

    private AttributeValue toStringAttributeValue(String value) {
        return AttributeValue.builder().s(value).build();
    }

    private AttributeValue toNumberAttributeValue(long value) {
        return AttributeValue.builder().n(String.valueOf(value)).build();
    }

    private AttributeValue toMapAttributeValue(JobMessage jobMessage) {
        Map<String, AttributeValue> message = new HashMap<>();
        message.put(MSG_LEVEL.key(), toStringAttributeValue(jobMessage.getLevel().getKey()));
        message.put(MSG_TEXT.key(), toStringAttributeValue(jobMessage.getMessage()));
        message.put(MSG_TS.key(), toStringAttributeValue(jobMessage.getTimestamp()));
        return AttributeValue.builder()
                .m(message)
                .build();
    }

    private AttributeValue messagesToAttributeValueList(List<JobMessage> jobeMessages) {
        final List<AttributeValue> messageAttributes = jobeMessages.stream().map(this::toMapAttributeValue).collect(toList());
        return toAttributeValueList(messageAttributes);
    }

    private AttributeValue toAttributeValueList(List<AttributeValue> values) {
        return AttributeValue.builder().l(values).build();
    }

}
