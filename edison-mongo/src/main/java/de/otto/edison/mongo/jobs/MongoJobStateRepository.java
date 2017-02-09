package de.otto.edison.mongo.jobs;

import com.mongodb.BasicDBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import de.otto.edison.jobs.repository.JobStateRepository;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Set;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

public class MongoJobStateRepository implements JobStateRepository {

    private static final FindOneAndUpdateOptions UPSERT = new FindOneAndUpdateOptions().upsert(true);
    private static final String JOBSTATE_COLLECTION_NAME = "jobstate";
    private static final String ID = "_id";

    private final MongoCollection<Document> collection;

    public MongoJobStateRepository(final MongoDatabase database) {
        this.collection = database.getCollection(JOBSTATE_COLLECTION_NAME);
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
        return "MongoJobStateRepository";
    }
}
