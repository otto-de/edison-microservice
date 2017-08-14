package de.otto.edison.dynamodb.jobs;

import de.otto.edison.annotations.Beta;
import de.otto.edison.dynamodb.AbstractDynamoRepository;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;
import software.amazon.awssdk.services.dynamodb.document.*;
import software.amazon.awssdk.services.dynamodb.document.spec.ScanSpec;
import software.amazon.awssdk.services.dynamodb.document.spec.UpdateItemSpec;
import software.amazon.awssdk.services.dynamodb.document.utils.ValueMap;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableSet;
import static software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbMapperFieldModel.DynamoDbAttributeType.S;
import static software.amazon.awssdk.services.dynamodb.model.KeyType.HASH;

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

    private final DynamoDBClient dynamoClient;
    private final Table table;

    public DynamoJobMetaRepository(final DynamoDBClient dynamoClient, final String jobMetaCollectionName) {
        this.dynamoClient = dynamoClient;
        table = new DynamoDb(dynamoClient).getTable(jobMetaCollectionName);
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
                .valueMap(new ValueMap().withString(":v", value));
        table().updateItem(updateItemSpec);
        return value;
    }

    private void fetchItemFromDynamoDbOrCreateDefault(final String jobType) {
        final Item item = table().getItem(FIELD_JOB_TYPE, jobType);
        if (null == item) {
            final Item defaultItem = new Item()
                    .withPrimaryKey(FIELD_JOB_TYPE, jobType)
                    .withMap(FIELD_META, new ConcurrentHashMap<>());
            table().putItem(defaultItem);
            return;
        }
        if (null == item.getMap(FIELD_META)) {
            final UpdateItemSpec updateItemSpec = new UpdateItemSpec().withPrimaryKey(FIELD_JOB_TYPE, jobType)
                    .withUpdateExpression("set " + FIELD_META + " = :v")
                    .valueMap(new ValueMap().withMap(":v", new ConcurrentHashMap<>()));
            table().updateItem(updateItemSpec);
        }
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

    void createTable() {
        if (!dynamoClient.listTables().tableNames().contains(table().getTableName())) {
            dynamoClient.createTable(CreateTableRequest.builder()
                    .tableName(table().getTableName())
                    .keySchema(KeySchemaElement.builder().attributeName(getKeyFieldName()).keyType(HASH).build())
                    .provisionedThroughput(ProvisionedThroughput.builder()
                            .readCapacityUnits(1000L)
                            .writeCapacityUnits(1000L)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName(getKeyFieldName())
                            .attributeType(S.name())
                            .build())
                    .build());
        }
    }
}
