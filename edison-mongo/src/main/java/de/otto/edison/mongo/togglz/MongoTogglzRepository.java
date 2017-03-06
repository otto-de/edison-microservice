package de.otto.edison.mongo.togglz;

import static org.springframework.util.StringUtils.isEmpty;

import java.util.Map;
import java.util.Optional;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.UserProvider;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.otto.edison.mongo.AbstractMongoRepository;
import de.otto.edison.togglz.FeatureClassProvider;

public class MongoTogglzRepository extends AbstractMongoRepository<String, FeatureState> implements StateRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MongoTogglzRepository.class);

    private static final String NAME = "_id";
    private static final String ENABLED = "enabled";
    private static final String STRATEGY = "strategy";
    private static final String PARAMETERS = "parameters";

    private final MongoCollection<Document> collection;
    private final FeatureClassProvider featureClassProvider;
    private final UserProvider userProvider;

    public MongoTogglzRepository(final MongoDatabase mongoDatabase,
                                 final FeatureClassProvider featureClassProvider,
                                 final UserProvider userProvider) {
        this.featureClassProvider = featureClassProvider;
        this.collection = mongoDatabase.getCollection("togglz");
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
        LOG.info((!isEmpty(userProvider.getCurrentUser().getName()) ? "User '" + userProvider.getCurrentUser().getName() + "'" : "Unknown user")
                + (featureState.isEnabled() ? " enabled " : " disabled ") + "feature " + featureState.getFeature().name());
    }

    @Override
    protected MongoCollection<Document> collection() {
        return collection;
    }

    @Override
    protected String keyOf(final FeatureState value) {
        return value.getFeature().name();
    }

    @Override
    protected Document encode(final FeatureState value) {
        final Document document = new Document();

        document.append(NAME, value.getFeature().name());
        document.append(ENABLED, value.isEnabled());
        document.append(STRATEGY, value.getStrategyId());
        document.append(PARAMETERS, value.getParameterMap());

        return document;
    }

    @Override
    protected FeatureState decode(final Document document) {
        final String name = document.getString(NAME);
        final Boolean enabled = document.getBoolean(ENABLED);
        final String strategy = document.getString(STRATEGY);
        final Map<String, String> parameters = document.get(PARAMETERS, Map.class);

        final FeatureState featureState = new FeatureState(resolveEnumValue(name));
        featureState.setEnabled(enabled);
        featureState.setStrategyId(strategy);
        for (final Map.Entry<String, String> parameter : parameters.entrySet()) {
            featureState.setParameter(parameter.getKey(), parameter.getValue());
        }

        return featureState;
    }

    @Override
    protected void ensureIndexes() {
        // no indices
    }

    private Feature resolveEnumValue(final String name) {
        final Class enumType = featureClassProvider.getFeatureClass();
        return (Feature) Enum.valueOf(enumType, name);
    }
}
