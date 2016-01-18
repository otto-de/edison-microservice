package de.otto.edison.togglz.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.mongo.AbstractMongoRepository;
import de.otto.edison.togglz.FeatureClassProvider;
import de.otto.edison.togglz.domain.FeatureDTO;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.metadata.FeatureGroup;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class MongoFeatureRepository extends AbstractMongoRepository<String, FeatureState> implements StateRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MongoFeatureRepository.class);

    private static final String NAME = "_id";
    private static final String ENABLED = "enabled";
    private static final String STRATEGY = "strategy";
    private static final String PARAMETERS = "parameters";

    private final MongoCollection<Document> collection;
    private final FeatureClassProvider featureClassProvider;

    @Autowired
    public MongoFeatureRepository(final MongoDatabase database,
                                  final FeatureClassProvider featureClassProvider) {
        this.collection = database.getCollection("togglz");
        this.featureClassProvider = featureClassProvider;
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {
        Optional<FeatureState> featureState = findOne(feature.name());
        if (!featureState.isPresent()) {
            return null;
        }

        return featureState.get();
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        createOrUpdate(featureState);
        LOG.info("switched feature toggle '{}' to '{}'", featureState.getFeature().name(), featureState.isEnabled());
    }

    public List<FeatureDTO> loadAll(){
        final List<FeatureDTO> featureStates = collection().find().map(this::create).into(new ArrayList<>());
        final List<FeatureDTO> featuresInDbAndEnum = featureStates.stream().filter(f -> inEnum(f)).collect(Collectors.toList());
        final List<FeatureDTO> featureOnlyInEnum = getFeatures().stream().filter(f -> featureNameNotInList(featureStates, f))
                .map(this::fromEnum).collect(Collectors.toList());
        featuresInDbAndEnum.addAll(featureOnlyInEnum);
        return featuresInDbAndEnum;
    }

    private boolean inEnum(final FeatureDTO dto){
        try{
            return resolveEnumValue(dto.getName()) != null;
        }catch(final IllegalArgumentException e){
            return false;
        }
    }

    private FeatureDTO fromEnum(final Feature feature){
        final FeatureDTO dto = new FeatureDTO();
        dto.setActive(false);
        dto.setName(feature.name());
        dto.setDescription(getDescription(feature));
        dto.setGroup(getGroupFromFeature(feature));
        return dto;
    }

    private boolean featureNameNotInList(final List<FeatureDTO> featureStates, final Feature feature){
        return !featureStates.stream().map(f -> f.getName()).collect(Collectors.toList()).contains(feature.name());
    }

    private FeatureDTO create(Document doc){
        FeatureState featureState = this.decode(doc);

        FeatureDTO featureDTO = new FeatureDTO();
        featureDTO.setActive(featureState.isEnabled());
        featureDTO.setName(featureState.getFeature().name());

        featureDTO.setDescription(getDescription(featureState.getFeature()));
        featureDTO.setGroup(getGroupFromFeature(featureState.getFeature()));

        return featureDTO;
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
        Document document = new Document();

        document.append(NAME, value.getFeature().name());
        document.append(ENABLED, value.isEnabled());
        document.append(STRATEGY, value.getStrategyId());

        return document;
    }

    @Override
    protected FeatureState decode(final Document document) {
        final String name = document.getString(NAME);
        final Boolean enabled = document.getBoolean(ENABLED);
        final String strategy = document.getString(STRATEGY);

        final FeatureState featureState = new FeatureState(resolveEnumValue(name));
        featureState.setEnabled(enabled);
        featureState.setStrategyId(strategy);
        return featureState;
    }

    private Set<Feature> getFeatures(){
        return FeatureContext.getFeatureManager().getFeatures();
    }
    @Override
    protected void ensureIndexes() {
        // no indices
    }

    private String getGroupFromFeature(final Feature feature){
        final Set<FeatureGroup> groups = FeatureContext.getFeatureManager().getMetaData(feature).getGroups();
        if(groups == null || groups.isEmpty()){
            return "";
        }
        return groups.iterator().next().getLabel();
    }

    public String getDescription(final Feature feature){
        return FeatureContext.getFeatureManager().getMetaData(feature).getLabel();
    }

    private Feature resolveEnumValue(String name) {
        final Class enumType = featureClassProvider.getFeatureClass();
        return (Feature) Enum.valueOf(enumType, name);
    }
}
