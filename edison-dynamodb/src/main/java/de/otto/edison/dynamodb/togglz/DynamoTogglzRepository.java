package de.otto.edison.dynamodb.togglz;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static com.amazonaws.services.dynamodbv2.model.KeyType.HASH;
import static com.amazonaws.services.dynamodbv2.model.ScalarAttributeType.S;
import static java.util.Collections.singletonList;
import static org.springframework.util.StringUtils.isEmpty;

@ConditionalOnMissingBean(StateRepository.class)
@Beta
public class DynamoTogglzRepository extends AbstractDynamoRepository<FeatureState> implements StateRepository {

    private static final Logger LOG = LoggerFactory.getLogger(DynamoTogglzRepository.class);

    private static final String ENABLED = "enabled";
    private static final String STRATEGY = "strategy";
    private static final String PARAMETERS = "parameters";

    private final AmazonDynamoDB dynamoClient;
    private final Table table;
    private final FeatureClassProvider featureClassProvider;
    private final UserProvider userProvider;

    public DynamoTogglzRepository(final AmazonDynamoDB dynamoClient, final DynamoDB dynamoDatabase, final FeatureClassProvider featureClassProvider, final UserProvider userProvider) {
        this.dynamoClient = dynamoClient;
        table = dynamoDatabase.getTable("togglz");
        this.featureClassProvider = featureClassProvider;
        this.userProvider = userProvider;
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
        return table;
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
    @SuppressWarnings("unchecked")
    protected FeatureState decode(final Item item) {
        final String name = item.getString(getKeyFieldName());
        final Boolean enabled = item.getBoolean(ENABLED);
        final String strategy = item.getString(STRATEGY);
        final Map<String, String> parameters = item.getMap(PARAMETERS);

        final FeatureState featureState = new FeatureState(resolveEnumValue(name));
        featureState.setEnabled(enabled);
        featureState.setStrategyId(strategy);
        for (final Entry<String, String> parameter : parameters.entrySet()) {
            featureState.setParameter(parameter.getKey(), parameter.getValue());
        }

        return featureState;
    }

    @Override
    protected String getKeyFieldName() {
        return "id";
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Feature resolveEnumValue(final String name) {
        final Class enumType = featureClassProvider.getFeatureClass();
        return (Feature) Enum.valueOf(enumType, name);
    }

    void createTable() {
        if (!dynamoClient.listTables().getTableNames().contains(table().getTableName())) {
            final ProvisionedThroughput provisionedThroughput = new ProvisionedThroughput();
            provisionedThroughput.setReadCapacityUnits(1000L);
            provisionedThroughput.setWriteCapacityUnits(1000L);
            dynamoClient.createTable(singletonList(new AttributeDefinition(getKeyFieldName(), S)), table().getTableName(),
                    singletonList(new KeySchemaElement(getKeyFieldName(), HASH)), provisionedThroughput);
        }
    }
}
