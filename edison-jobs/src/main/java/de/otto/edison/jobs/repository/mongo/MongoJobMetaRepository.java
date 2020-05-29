package de.otto.edison.jobs.repository.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 * {@inheritDoc}
 * <p>
 * MongoDB implementation of the JobMetaRepository.
 *
 * </p>
 */
public class MongoJobMetaRepository implements JobMetaRepository {

    private static final FindOneAndUpdateOptions UPSERT = new FindOneAndUpdateOptions()
            .upsert(true)
            .maxTime(250, TimeUnit.MILLISECONDS);
    private static final String ID = "_id";
    private static final String KEY_DISABLED = "_e_disabled";
    private static final String KEY_RUNNING = "_e_running";

    private final MongoCollection<Document> collection;
    private final MongoProperties mongoProperties;

    public MongoJobMetaRepository(final MongoDatabase mongoDatabase,
                                  final String jobMetaCollectionName,
                                  final MongoProperties mongoProperties) {
        this.mongoProperties = mongoProperties;
        MongoCollection<Document> tmpCollection = mongoDatabase.getCollection(jobMetaCollectionName);
        collection = tmpCollection.withWriteConcern(tmpCollection
                .getWriteConcern()
                .withWTimeout(mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS));
    }

    @Override
    public JobMeta getJobMeta(final String jobType) {
        final Document document = collection
                .find(eq(ID, jobType))
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
                .first();
        if (document != null) {
            final Map<String, String> meta = document.keySet()
                    .stream()
                    .filter(key -> !key.startsWith("_e_") && !key.equals(ID))
                    .collect(toMap(
                            key -> key,
                            document::getString
                    ));
            final boolean isRunning = document.containsKey(KEY_RUNNING);
            final boolean isDisabled = document.containsKey(KEY_DISABLED);
            final String comment = document.getString(KEY_DISABLED);
            return new JobMeta(jobType, isRunning, isDisabled, comment, meta);
        } else {
            return new JobMeta(jobType, false, false, "", emptyMap());
        }
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
    public String setValue(final String jobType,
                           final String key,
                           final String value) {
        final Document previous;
        if (value != null) {
            previous = collection.findOneAndUpdate(eq(ID, jobType), set(key, value), UPSERT);
        } else {
            previous = collection.findOneAndUpdate(eq(ID, jobType), unset(key), UPSERT);
        }
        return previous != null
                ? previous.getString("key")
                : null;
    }

    @Override
    public String getValue(final String jobType,
                           final String key) {
        final Document first = collection
                .find(eq(ID, jobType))
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
                .first();
        return first != null ? first.getString(key) : null;
    }

    @Override
    public boolean createValue(final String jobType, final String key, final String value) {

        final Bson filter = and(
                eq(ID, jobType),
                exists(key, false));

        final Bson update = set(key, value);
        try {
            final Document previous = collection.findOneAndUpdate(filter, update, UPSERT);
            return previous == null || previous.getString(key) == null;
        } catch (final Exception e) {
            return false;
        }
    }

    /**
     * Returns all job types having state information.
     *
     * @return set containing job types.
     */
    @Override
    public Set<String> findAllJobTypes() {
        return stream(collection.find().maxTime(500, TimeUnit.MILLISECONDS).spliterator(), false)
                .map(doc -> doc.getString(ID))
                .collect(toSet());
    }

    @Override
    public void deleteAll() {
        collection.deleteMany(new BasicDBObject());
    }

    @Override
    public String toString() {
        return "MongoJobMetaRepository";
    }
}
