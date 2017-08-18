package de.otto.edison.dynamodb.jobs;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.*;
import de.otto.edison.annotations.Beta;
import de.otto.edison.dynamodb.AbstractDynamoRepository;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.JobRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.amazonaws.services.dynamodbv2.model.KeyType.HASH;
import static com.amazonaws.services.dynamodbv2.model.KeyType.RANGE;
import static com.amazonaws.services.dynamodbv2.model.ProjectionType.ALL;
import static com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.N;
import static com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.S;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

@Beta
public class DynamoJobRepository extends AbstractDynamoRepository<JobInfo> implements JobRepository {

    private static final String INDEX_LATEST_PER_TYPE = "latestPerTypeIndex";
    private static final String INDEX_STARTED = "startedIndex";

    private static final String FIELD_CONSTANT_VALUE = "constantValue";
    private static final String FIELD_ID = "id";
    private static final String FIELD_JOBTYPE = "jobType";
    private static final String FIELD_STATUS = "jobStatus";
    private static final String FIELD_MESSAGES = "messages";
    private static final String FIELD_LAST_UPDATED = "lastUpdated";
    private static final String FIELD_HOSTNAME = "hostname";
    private static final String FIELD_STARTED = "started";
    private static final String FIELD_STOPPED = "stopped";
    private static final List<String> FIELDS_WITHOUT_MESSAGES = Arrays.asList(
            FIELD_ID, FIELD_JOBTYPE, FIELD_STATUS, FIELD_LAST_UPDATED,
            FIELD_HOSTNAME, FIELD_STARTED, FIELD_STOPPED);

    private final AmazonDynamoDB dynamoClient;
    private final String jobInfoCollectionName;

    public DynamoJobRepository(final AmazonDynamoDB dynamoClient, final String jobInfoCollectionName) {
        this.dynamoClient = dynamoClient;
        this.jobInfoCollectionName = jobInfoCollectionName;
    }

    @Override
    public JobStatus findStatus(final String jobId) {
        return findOne(jobId).get().getStatus();
    }

    @Override
    public void removeIfStopped(final String id) {
        findOne(id).ifPresent(jobInfo -> {
            if (jobInfo.isStopped()) {
                delete(id);
            }
        });
    }

    @Override
    public void appendMessage(final String jobId, final JobMessage jobMessage) {
        table().updateItem(new UpdateItemSpec()
                .withPrimaryKey(FIELD_ID, jobId)
                .withUpdateExpression("set " + FIELD_MESSAGES + " = list_append(" + FIELD_MESSAGES + ", :messages)")
                .withValueMap(new ValueMap()
                        .withList(":messages", singletonList(mapToItem(jobMessage)))));
    }

    @Override
    public void setJobStatus(final String jobId, final JobStatus jobStatus) {
        table().updateItem(getKeyFieldName(), jobId, new AttributeUpdate(FIELD_STATUS).put(jobStatus.name()));
    }

    @Override
    public void setLastUpdate(final String jobId, final OffsetDateTime lastUpdate) {
        table().updateItem(getKeyFieldName(), jobId, new AttributeUpdate(FIELD_LAST_UPDATED)
                .put(lastUpdate.toInstant().toEpochMilli()));
    }

    @Override
    public List<JobInfo> findLatest(final int maxCount) {
        return toStream(table().getIndex(INDEX_STARTED).query(new QuerySpec()
                .withMaxResultSize(maxCount)
                .withScanIndexForward(false)
                .withKeyConditionExpression(FIELD_CONSTANT_VALUE + " = :val")
                .withValueMap(new ValueMap()
                        .withInt(":val", 1))))
                .map(this::decode)
                .sorted(comparing(JobInfo::getStarted))
                .collect(toList());
    }

    @Override
    public List<JobInfo> findLatestJobsDistinct() {
        final List<JobInfo> result = new ArrayList<>();
        final Index index = table().getIndex(INDEX_LATEST_PER_TYPE);
        final ScanSpec scanSpec = new ScanSpec()
                .withProjectionExpression(FIELD_JOBTYPE);
        final ItemCollection<ScanOutcome> items = index.scan(scanSpec);
        final HashSet<String> jobTypes = new HashSet<>();

        for (final Item item : items) {
            jobTypes.add(item.getString(FIELD_JOBTYPE));
        }
        for (final String jobType : jobTypes) {
            final QuerySpec spec = new QuerySpec()
                    .withMaxResultSize(1)
                    .withScanIndexForward(false)
                    .withKeyConditionExpression(FIELD_JOBTYPE + " = :val")
                    .withValueMap(new ValueMap()
                            .withString(":val", jobType)
                    );
            final ItemCollection<QueryOutcome> query = index.query(spec);
            result.addAll(toStream(query)
                    .map(this::decode)
                    .sorted(comparing(JobInfo::getStarted))
                    .collect(toList()));
        }
        return result;
    }

    @Override
    public List<JobInfo> findLatestBy(final String type, final int maxCount) {
        final Index index = table().getIndex(INDEX_LATEST_PER_TYPE);
        final ItemCollection<QueryOutcome> items = index.query(new QuerySpec().withHashKey(FIELD_JOBTYPE, type)
                .withScanIndexForward(false)
                .withMaxResultSize(maxCount));
        return toStream(items)
                .map(this::decode)
                .sorted(comparing(JobInfo::getStarted))
                .collect(toList());
    }

    @Override
    public List<JobInfo> findByType(final String type) {
        return toStream(table().getIndex(INDEX_LATEST_PER_TYPE).query(new QuerySpec().withHashKey(FIELD_JOBTYPE, type)
                .withScanIndexForward(false)))
                .map(this::decode)
                .sorted(comparing(JobInfo::getStarted))
                .collect(toList());
    }

    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(final OffsetDateTime timeOffset) {
        return toStream(table().scan(new ScanSpec()
                .withFilterExpression("attribute_not_exists(stopped) and lastUpdated < :time")
                .withValueMap(new ValueMap()
                        .withLong(":time", timeOffset.toInstant().toEpochMilli()))))
                .map(this::decode)
                .sorted(comparing(JobInfo::getStarted))
                .collect(toList());
    }

    @Override
    protected final Item encode(final JobInfo job) {
        final Item item = new Item().withPrimaryKey(getKeyFieldName(), keyOf(job))
                .withInt(FIELD_CONSTANT_VALUE, 1)
                .withString(FIELD_JOBTYPE, job.getJobType())
                .with(FIELD_STARTED, job.getStarted().toInstant().toEpochMilli())
                .with(FIELD_LAST_UPDATED, job.getLastUpdated().toInstant().toEpochMilli())
                .withList(FIELD_MESSAGES, mapToItems(job.getMessages()))
                .withString(FIELD_STATUS, job.getStatus().name())
                .withString(FIELD_HOSTNAME, job.getHostname());
        if (job.isStopped()) {
            item.with(FIELD_STOPPED, job.getStopped().get().toInstant().toEpochMilli());
        }
        return item;
    }

    private List<Map<String, Object>> mapToItems(final List<JobMessage> messages) {
        return messages.stream()
                .map(this::mapToItem)
                .collect(toList());
    }

    private Map<String, Object> mapToItem(final JobMessage jobMessage) {
        final Map<String, Object> result = new HashMap<>();
        result.put("level", jobMessage.getLevel().name());
        result.put("message", jobMessage.getMessage());
        result.put("timestamp", jobMessage.getTimestamp().toInstant().toEpochMilli());
        return result;
    }

    @Override
    protected final JobInfo decode(final Item item) {
        return JobInfo.builder()
                .setJobId(item.getString(FIELD_ID))
                .setJobType(item.getString(FIELD_JOBTYPE))
                .setHostname(item.getString(FIELD_HOSTNAME))
                .setMessages(mapMessages(item.getList(FIELD_MESSAGES)))
                .setStarted(isAttrSet(item, FIELD_STARTED) ? mapOffsetDateTime(item.getLong(FIELD_STARTED)) : mapOffsetDateTime(0))
                .setStopped(isAttrSet(item, FIELD_STOPPED) ? mapOffsetDateTime(item.getLong(FIELD_STOPPED)) : null)
                .setStatus(JobStatus.valueOf(item.getString(FIELD_STATUS)))
                .setLastUpdated(mapOffsetDateTime(item.getLong(FIELD_LAST_UPDATED)))
                .setClock(Clock.systemDefaultZone())
                .build();
    }

    private boolean isAttrSet(final Item item, final String key) {
        return item.isPresent(key) && item.get(key) != null;
    }

    @Override
    protected String getKeyFieldName() {
        return "id";
    }

    private List<JobMessage> mapMessages(final List<Map<String, Object>> messages) {
        if (messages == null) {
            return emptyList();
        }
        return messages.stream()
                .map(this::mapMessage)
                .collect(toList());
    }

    private JobMessage mapMessage(final Map<String, Object> msg) {
        final OffsetDateTime offsetDateTime = mapOffsetDateTime(((BigDecimal) msg.get("timestamp")).longValue());
        final String message = (String) msg.get("message");
        final Level level = Level.valueOf((String) msg.get("level"));
        return jobMessage(level, message, offsetDateTime);
    }

    private OffsetDateTime mapOffsetDateTime(final long millis) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
    }

    @Override
    protected String tableName() {
        return jobInfoCollectionName;
    }

    @Override
    protected final String keyOf(final JobInfo value) {
        return value.getJobId();
    }

    @Override
    public List<JobInfo> findAllJobInfoWithoutMessages() {
        final ItemCollection<ScanOutcome> items = table().scan(new ScanSpec()
                .withProjectionExpression(join(", ", FIELDS_WITHOUT_MESSAGES)));
        return toStream(items)
                .map(this::decode)
                .sorted(comparing(JobInfo::getStarted))
                .collect(toList());
    }

    void createTable() {
        if (!dynamoClient.listTables().getTableNames().contains(table().getTableName())) {
            final ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput();
            provisionedThroughput.setReadCapacityUnits(1L);
            provisionedThroughput.setWriteCapacityUnits(1L);
            dynamoClient.createTable(singletonList(new AttributeDefinition(getKeyFieldName(), S)), table().getTableName(),
                    singletonList(new KeySchemaElement(getKeyFieldName(), HASH)),
                    provisionedThroughput);
            createStartedIndex();
            createLatestPerTypeIndex();
        }
    }

    private void createStartedIndex() {
        final Index gsi = table().createGSI(new CreateGlobalSecondaryIndexAction()
                        .withIndexName(INDEX_STARTED)
                        .withKeySchema(new KeySchemaElement(FIELD_CONSTANT_VALUE, HASH), new KeySchemaElement(FIELD_STARTED, RANGE))
                        .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
                        .withProjection(new Projection().withProjectionType(ALL)), new AttributeDefinition(FIELD_CONSTANT_VALUE, N),
                new AttributeDefinition(FIELD_STARTED, N));
        try {
            gsi.waitForActive();
        } catch (final InterruptedException | ResourceNotFoundException ignored) {
        }
    }

    private void createLatestPerTypeIndex() {
        final Index gsi = table().createGSI(new CreateGlobalSecondaryIndexAction()
                        .withIndexName(INDEX_LATEST_PER_TYPE)
                        .withKeySchema(new KeySchemaElement(FIELD_JOBTYPE, HASH), new KeySchemaElement(FIELD_STARTED, RANGE))
                        .withProvisionedThroughput(new ProvisionedThroughput(1L, 1L))
                        .withProjection(new Projection().withProjectionType(ALL)), new AttributeDefinition(FIELD_JOBTYPE, S),
                new AttributeDefinition(FIELD_STARTED, N));
        try {
            gsi.waitForActive();
        } catch (final InterruptedException | ResourceNotFoundException ignored) {
        }
    }
}
