package de.otto.edison.dynamodb.togglz;

import de.otto.edison.annotations.Beta;
import de.otto.edison.dynamodb.AbstractDynamoRepository;
import de.otto.edison.togglz.FeatureClassProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDBClient;
import software.amazon.awssdk.services.dynamodb.document.DynamoDb;
import software.amazon.awssdk.services.dynamodb.document.Item;
import software.amazon.awssdk.services.dynamodb.document.Table;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

import java.util.Map;
import java.util.Optional;

import static org.springframework.util.StringUtils.isEmpty;
import static software.amazon.awssdk.services.dynamodb.datamodeling.DynamoDbMapperFieldModel.DynamoDbAttributeType.S;
import static software.amazon.awssdk.services.dynamodb.model.KeyType.HASH;

@ConditionalOnMissingBean(StateRepository.class)
@Beta
public class DynamoTogglzRepository extends AbstractDynamoRepository<FeatureState> implements StateRepository {

    private static final Logger LOG = LoggerFactory.getLogger(DynamoTogglzRepository.class);

    private static final String ENABLED = "enabled";
    private static final String STRATEGY = "strategy";
    private static final String PARAMETERS = "parameters";

    private final FeatureClassProvider featureClassProvider;
    private final DynamoDBClient dynamoClient;
    private final Table table;
    private final UserProvider userProvider;

    public DynamoTogglzRepository(final DynamoDBClient dynamoClient,
                                  final FeatureClassProvider featureClassProvider,
                                  final UserProvider userProvider) {
        this.featureClassProvider = featureClassProvider;
        this.userProvider = userProvider;
        this.dynamoClient = dynamoClient;
        this.table = new DynamoDb(dynamoClient).getTable("togglz");
    }

    /**
     * Get the persisted state of a feature from the repository. If the repository doesn't contain any information regarding
     * this feature it must return <code>null</code>.
     *
     * @param feature The feature to read the state for
     * @return The persisted feature state or <code>null</code>
     */
    @Override
    public FeatureState getFeatureState(final Feature feature) {
        final Optional<FeatureState> featureState = findOne(feature.name());
        return featureState.orElse(null);
    }

    /**
     * Persist the supplied feature state. The repository implementation must ensure that subsequent calls to
     * {@link #getFeatureState(Feature)} return the same state as persisted using this method.
     *
     * @param featureState The feature state to persist
     * @throws UnsupportedOperationException if this state repository does not support updates
     */
    @Override
    public void setFeatureState(final FeatureState featureState) {
        createOrUpdate(featureState);
        LOG.info((!isEmpty(userProvider.getCurrentUser().getName()) ?
                "User '" + userProvider.getCurrentUser().getName() + "'" :
                "Unknown user")
                + (featureState.isEnabled() ? " enabled " : " disabled ") + "feature " + featureState.getFeature().name());
    }

    @Override
    protected Table table() {
        return this.table;
    }

    @Override
    protected String keyOf(final FeatureState value) {
        return value.getFeature().name();
    }

    @Override
    protected Item encode(final FeatureState value) {
        final Item item = new Item();

        item.withPrimaryKey(getKeyFieldName(), keyOf(value))
                .withString(STRATEGY, value.getStrategyId())
                .withBoolean(ENABLED, value.isEnabled())
                .withMap(PARAMETERS, value.getParameterMap());

        return item;
    }

    @Override
    protected FeatureState decode(final Item item) {
        final String name = item.getString(getKeyFieldName());
        final Boolean enabled = item.getBoolean(ENABLED);
        final String strategy = item.getString(STRATEGY);
        final Map<String, String> parameters = item.getMap(PARAMETERS);

        final FeatureState featureState = new FeatureState(resolveEnumValue(name));
        featureState.setEnabled(enabled);
        featureState.setStrategyId(strategy);
        for (final Map.Entry<String, String> parameter : parameters.entrySet()) {
            featureState.setParameter(parameter.getKey(), parameter.getValue());
        }

        return featureState;
    }

    @Override
    protected String getKeyFieldName() {
        return "id";
    }

    private Feature resolveEnumValue(final String name) {
        final Class enumType = featureClassProvider.getFeatureClass();
        return (Feature) Enum.valueOf(enumType, name);
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
