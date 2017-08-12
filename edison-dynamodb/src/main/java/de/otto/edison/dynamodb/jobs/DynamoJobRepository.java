package de.otto.edison.dynamodb.jobs;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import static com.amazonaws.services.dynamodbv2.model.KeyType.HASH;
import static com.amazonaws.services.dynamodbv2.model.KeyType.RANGE;
import static com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.N;
import static com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.S;

import static de.otto.edison.jobs.domain.JobMessage.jobMessage;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.AttributeUpdate;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Index;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.QueryOutcome;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.TableKeysAndAttributes;
import com.amazonaws.services.dynamodbv2.document.spec.BatchGetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateGlobalSecondaryIndexAction;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.Projection;
import com.amazonaws.services.dynamodbv2.model.ProjectionType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;

import de.otto.edison.annotations.Beta;
import de.otto.edison.dynamodb.AbstractDynamoRepository;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.JobRepository;

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

  private static final String NO_LOG_MESSAGE_FOUND = "No log message found";

  private final Table table;
  private final AmazonDynamoDB amazonDynamoDB;

  public DynamoJobRepository(final AmazonDynamoDB dynamoDB, final String jobInfoCollectionName) {
    this.table = new DynamoDB(dynamoDB).getTable(jobInfoCollectionName);
    this.amazonDynamoDB = dynamoDB;
  }

  @Override
  public JobStatus findStatus(final String jobId) {
    final JobInfo foundJobInfo = findOne(jobId).get();
    return foundJobInfo.getStatus();
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
    table.updateItem(
      FIELD_ID,
      jobId,
      "SET #nam = list_append(#nam, :val)",
      Collections.singletonMap("#nam", FIELD_MESSAGES),
      Collections.singletonMap(":val", Collections.singletonList(mapToItem(jobMessage))));
  }

  @Override
  public void setJobStatus(final String jobId, final JobStatus jobStatus) {
    table().updateItem(getKeyFieldName(), jobId, new AttributeUpdate(FIELD_STATUS).put(jobStatus.name()));
  }

  @Override
  public void setLastUpdate(final String jobId, final OffsetDateTime lastUpdate) {
    table().updateItem(getKeyFieldName(), jobId,
      new AttributeUpdate(FIELD_LAST_UPDATED).put(lastUpdate.toInstant().toEpochMilli()));
  }

  @Override
  public List<JobInfo> findLatest(final int maxCount) {
    final Index index = table().getIndex(INDEX_STARTED);
    final QuerySpec spec = new QuerySpec()
      .withMaxResultSize(maxCount)
      .withScanIndexForward(false)
      .withKeyConditionExpression(FIELD_CONSTANT_VALUE + " = :val")
      .withValueMap(new ValueMap()
        .withInt(":val", 1));
    final ItemCollection<QueryOutcome> items = index.query(spec);

    return findMany(toStream(items)
      .map(item -> item.getString(getKeyFieldName()))
      .collect(toList()));
  }

  @Override
  public List<JobInfo> findLatestJobsDistinct() {
    final List<String> latestJobIdyPerType = findLatestJobIdsDistinct();
    return findMany(latestJobIdyPerType);
  }

  public List<String> findLatestJobIdsDistinct() {
    final List<String> result = new ArrayList<>();
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
        .map(item -> item.getString(getKeyFieldName()))
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
    return findMany(toStream(items)
      .map(item -> item.getString(getKeyFieldName()))
      .collect(toList()));
  }

  @Override
  public List<JobInfo> findByType(final String type) {
    final Index index = table().getIndex(INDEX_LATEST_PER_TYPE);

    final ItemCollection<QueryOutcome> items = index.query(new QuerySpec().withHashKey(FIELD_JOBTYPE, type)
      .withScanIndexForward(false));

    return findMany(toStream(items)
      .map(item -> item.getString(getKeyFieldName()))
      .collect(toList()));
  }

  @Override
  public List<JobInfo> findRunningWithoutUpdateSince(final OffsetDateTime timeOffset) {
    final ItemCollection<ScanOutcome> items = table().scan(
      new ScanSpec().withFilterExpression("attribute_not_exists(stopped) and lastUpdated < :time")
        .withValueMap(new ValueMap()
          .withLong(":time", timeOffset.toInstant().toEpochMilli())));

    return mapJobInfos(items);
  }

  private List<JobInfo> findMany(final List<String> jobIds) {
    if (jobIds.isEmpty()) {
      // BatchGet braucht mindestens einen Key
      return emptyList();
    }

    final BatchGetItemSpec spec = new BatchGetItemSpec()
      .withTableKeyAndAttributes(new TableKeysAndAttributes(table().getTableName())
        .withHashOnlyKeys(getKeyFieldName(), jobIds.toArray()));

    return new DynamoDB(amazonDynamoDB).batchGetItem(spec)
      .getTableItems()
      .entrySet()
      .stream()
      .flatMap(x -> mapJobInfos(x.getValue()).stream())
      .sorted(comparing(JobInfo::getStarted))
      .collect(toList());
  }

  private List<JobInfo> mapJobInfos(final ItemCollection<ScanOutcome> items) {
    return toStream(items)
      .map(this::decode)
      .sorted(comparing(JobInfo::getStarted))
      .collect(toList());
  }

  private List<JobInfo> mapJobInfos(final List<Item> items) {
    return items.stream()
      .map(this::decode)
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
      .collect(Collectors.toList());
  }

  private Map<String, Object> mapToItem(final JobMessage jobMessage) {
    final HashMap<String, Object> result = new HashMap<>();
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
      .setStatus(JobInfo.JobStatus.valueOf(item.getString(FIELD_STATUS)))
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
      return Collections.emptyList();
    }
    return messages.stream()
      .map(this::mapMessage)
      .collect(Collectors.toList());
  }

  private JobMessage mapMessage(final Map<String, Object> msg) {
    final OffsetDateTime offsetDateTime = mapOffsetDateTime(((BigDecimal) msg.get("timestamp")).longValue());
    final String message = (String) msg.get("message");
    final Level level = Level.valueOf((String) msg.get("level"));
    return JobMessage.jobMessage(level, message, offsetDateTime);
  }

  private OffsetDateTime mapOffsetDateTime(final long millis) {
    return OffsetDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault());
  }

  private JobMessage toJobMessage(final Item item) {
    return jobMessage(Level.valueOf(item.get(JobStructure.MSG_LEVEL.key()).toString()), getMessage(item),
      mapOffsetDateTime(item.getLong(JobStructure.MSG_TS.key())));
  }

  @Override
  protected Table table() {
    return this.table;
  }

  @Override
  protected final String keyOf(final JobInfo value) {
    return value.getJobId();
  }

  private String getMessage(final Item item) {
    return item.get(JobStructure.MSG_TEXT.key()) == null ?
      NO_LOG_MESSAGE_FOUND :
      item.get(JobStructure.MSG_TEXT.key()).toString();
  }

  @Override
  public List<JobInfo> findAllJobInfoWithoutMessages() {
    final ScanSpec scanSpec = new ScanSpec()
      .withProjectionExpression(String.join(", ", FIELDS_WITHOUT_MESSAGES));
    final ItemCollection<ScanOutcome> items = table.scan(scanSpec);
    return mapJobInfos(items);
  }

  public void createTable() {
    if (!amazonDynamoDB.listTables().getTableNames().contains(table().getTableName())) {
      final ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput();
      provisionedThroughput.setReadCapacityUnits(1000L);
      provisionedThroughput.setWriteCapacityUnits(1000L);
      amazonDynamoDB.createTable(singletonList(new AttributeDefinition(getKeyFieldName(), S)), table().getTableName(),
        singletonList(new KeySchemaElement(getKeyFieldName(), HASH)),
        provisionedThroughput);
      createStartedIndex();
      createLatestPerTypeIndex();
    }
  }

  public void deleteTable() {
    if (amazonDynamoDB.listTables().getTableNames().contains(table().getTableName())) {
      amazonDynamoDB.deleteTable(table().getTableName());
    }
  }

  private void createStartedIndex() {
    final Index gsi = table().createGSI(new CreateGlobalSecondaryIndexAction()
        .withIndexName(INDEX_STARTED)
        .withKeySchema(new KeySchemaElement(FIELD_CONSTANT_VALUE, HASH), new KeySchemaElement(FIELD_STARTED, RANGE))
        .withProvisionedThroughput(new ProvisionedThroughput(1000L, 1000L))
        .withProjection(new Projection().withProjectionType(ProjectionType.ALL)), new AttributeDefinition(FIELD_CONSTANT_VALUE, N),
      new AttributeDefinition(FIELD_STARTED, N));
    try {
      gsi.waitForActive();
    } catch (final InterruptedException | ResourceNotFoundException e) {
    }
  }

  private void createLatestPerTypeIndex() {
    final Index gsi = table().createGSI(new CreateGlobalSecondaryIndexAction()
        .withIndexName(INDEX_LATEST_PER_TYPE)
        .withKeySchema(new KeySchemaElement(FIELD_JOBTYPE, HASH), new KeySchemaElement(FIELD_STARTED, RANGE))
        .withProvisionedThroughput(new ProvisionedThroughput(1000L, 1000L))
        .withProjection(new Projection().withProjectionType(ProjectionType.ALL)), new AttributeDefinition(FIELD_JOBTYPE, S),
      new AttributeDefinition(FIELD_STARTED, N));
    try {
      gsi.waitForActive();
    } catch (final InterruptedException | ResourceNotFoundException e) {
    }
  }
}
