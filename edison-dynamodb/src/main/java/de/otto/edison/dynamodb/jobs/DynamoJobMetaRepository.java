package de.otto.edison.dynamodb.jobs;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableSet;

import static com.amazonaws.services.dynamodbv2.model.KeyType.HASH;
import static com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.S;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;

import de.otto.edison.annotations.Beta;
import de.otto.edison.dynamodb.AbstractDynamoRepository;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;

/**
 * {@inheritDoc}
 * <p>
 * MongoDB implementation of the JobMetaRepository.
 * </p>
 */
@Beta
public class DynamoJobMetaRepository extends AbstractDynamoRepository<JobMeta> implements JobMetaRepository {

  private static final String KEY_DISABLED = "disabled";
  private static final String KEY_RUNNING = "running";

  private static final String FIELD_JOB_TYPE = "jobType";
  private static final String FIELD_META = "meta";

  private final AmazonDynamoDB dynamoDB;
  private final Table table;

  public DynamoJobMetaRepository(final AmazonDynamoDB dynamoDB, final String jobMetaCollectionName) {
    this.dynamoDB = dynamoDB;
    this.table = new DynamoDB(dynamoDB).getTable(jobMetaCollectionName);
  }

  @Override
  public JobMeta getJobMeta(final String jobType) {
    return findOne(jobType).orElse(new JobMeta(jobType, false, false, "", emptyMap()));
  }

  private Item getJobMetaItem(final String jobType) {
    if (jobType == null) {
      return null;
    }
    return table().getItem(FIELD_JOB_TYPE, jobType);
  }

  @Override
  public boolean setRunningJob(final String jobType, final String jobId) {
    return createValue(jobType, KEY_RUNNING, jobId);
  }

  @Override
  public String getRunningJob(final String jobType) {
    return getValue(jobType, KEY_RUNNING);
  }

  /**
   * Clears the job running mark of the jobType. Does nothing if not mark exists.
   *
   * @param jobType the job type
   */
  @Override
  public void clearRunningJob(final String jobType) {
    setValue(jobType, KEY_RUNNING, null);
  }

  /**
   * Disables a job type, i.e. prevents it from being started
   *
   * @param jobType the disabled job type
   * @param comment an optional comment
   */
  @Override
  public void disable(final String jobType, final String comment) {
    setValue(jobType, KEY_DISABLED, comment != null ? comment : "");
  }

  /**
   * Reenables a job type that was disabled
   *
   * @param jobType the enabled job type
   */
  @Override
  public void enable(final String jobType) {
    setValue(jobType, KEY_DISABLED, null);
  }

  @Override
  public String setValue(final String jobType, final String key, final String value) {
    fetchItemFromDynamoDbOrCreateDefault(jobType);
    final UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(FIELD_JOB_TYPE, jobType)
      .withUpdateExpression("set " + FIELD_META + "." + key + " = :v")
      .withValueMap(new ValueMap().withString(":v", value));
    table().updateItem(updateItemSpec);
    return value;
  }

  private Item fetchItemFromDynamoDbOrCreateDefault(final String jobType) {
    final Item item = table().getItem(FIELD_JOB_TYPE, jobType);
    if (null == item) {
      final Item defaultItem = new Item()
        .withPrimaryKey(FIELD_JOB_TYPE, jobType)
        .withMap(FIELD_META, new ConcurrentHashMap<>());
      table().putItem(defaultItem);
      return defaultItem;
    }
    if (null == item.getMap(FIELD_META)) {
      final UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(FIELD_JOB_TYPE, jobType)
        .withUpdateExpression("set " + FIELD_META + " = :v")
        .withValueMap(new ValueMap().withMap(":v", new ConcurrentHashMap<>()));
      table().updateItem(updateItemSpec);
    }
    return item;
  }

  @Override
  public String getValue(final String jobType,
                         final String key) {
    final Item jobMeta = getJobMetaItem(jobType);
    return jobMeta == null ? null : String.class.cast(jobMeta.getMap(FIELD_META).get(key));
  }

  @Override
  public boolean createValue(final String jobType, final String key, final String value) {
    if (getValue(jobType, key) != null) {
      return false;
    }
    setValue(jobType, key, value);
    return true;
  }

  /**
   * Returns all job types having state information.
   *
   * @return set containing job types.
   */
  @Override
  public Set<String> findAllJobTypes() {
    final Set<String> jobTypes = new HashSet<>();
    final ScanSpec scanSpec = new ScanSpec();
    final ItemCollection<ScanOutcome> items = table().scan(scanSpec);
    for (final Item item : items) {
      jobTypes.add(item.getString(FIELD_JOB_TYPE));
    }
    return unmodifiableSet(jobTypes);
  }

  @Override
  protected Table table() {
    return table;
  }

  @Override
  protected String keyOf(final JobMeta jobMeta) {
    return jobMeta.getJobType();
  }

  @Override
  protected Item encode(final JobMeta jobMeta) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected JobMeta decode(final Item item) {
    final Map<String, String> meta = item.getMap(FIELD_META) == null
      ? new ConcurrentHashMap<>()
      : item.getMap(FIELD_META);
    final boolean running = meta.containsKey(KEY_RUNNING);
    final boolean disabled = meta.containsKey(KEY_DISABLED);
    final String disableComment = meta.get(KEY_DISABLED);

    meta.remove(KEY_DISABLED);
    meta.remove(KEY_RUNNING);
    return new JobMeta(item.getString(FIELD_JOB_TYPE), running, disabled, disableComment, meta);
  }

  @Override
  protected String getKeyFieldName() {
    return FIELD_JOB_TYPE;
  }

  public void createTable() {
    if (!dynamoDB.listTables().getTableNames().contains(table().getTableName())) {
      final ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput();
      provisionedThroughput.setReadCapacityUnits(1000L);
      provisionedThroughput.setWriteCapacityUnits(1000L);
      dynamoDB.createTable(singletonList(new AttributeDefinition(getKeyFieldName(), S)), table().getTableName(),
        singletonList(new KeySchemaElement(getKeyFieldName(), HASH)),
        provisionedThroughput);
    }
  }

  public void deleteTable() {
    if (dynamoDB.listTables().getTableNames().contains(table().getTableName())) {
      dynamoDB.deleteTable(table().getTableName());
    }
  }
}
