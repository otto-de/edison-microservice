package de.otto.edison.mongo.jobs;

import com.mongodb.BasicDBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 * {@inheritDoc}
 * <p>
 *     MongoDB implementation of the JobMetaRepository.
 *
 * </p>
 */
public class MongoJobMetaRepository implements JobMetaRepository {

    private static final FindOneAndUpdateOptions UPSERT = new FindOneAndUpdateOptions().upsert(true);
    private static final String JOBMETA_COLLECTION_NAME = "jobmeta";
    private static final String ID = "_id";
    private static final String KEY_DISABLED = "_e_disabled";
    private static final String KEY_RUNNING = "_e_running";

    private final MongoCollection<Document> collection;

    public MongoJobMetaRepository(final MongoDatabase database) {
        this.collection = database.getCollection(JOBMETA_COLLECTION_NAME);
    }

    @Override
    public JobMeta getJobMeta(final String jobType) {
        final Document document = collection.find(eq(ID, jobType)).first();
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
        final Document first = collection.find(eq(ID, jobType)).first();
        return first != null ? first.getString(key) : null;
    }

    @Override
    public boolean createValue(String jobType, String key, String value) {

        final Bson filter = and(
                eq(ID, jobType),
                exists(key, false));

        final Bson update = set(key, value);
        try {
            final Document previous = collection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions().upsert(true));
            return previous == null || previous.getString(key) == null;
        } catch (final DuplicateKeyException e) {
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
        return stream(collection.find().spliterator(), false)
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
