package de.otto.edison.mongo.jobs;

import com.mongodb.DuplicateKeyException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.UpdateOptions;
import de.otto.edison.jobs.repository.JobStateRepository;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;

public class MongoJobStateRepository implements JobStateRepository {

    private static final String JOBSTATE_COLLECTION_NAME = "jobstate";
    private static final String ID = "_id";
    public static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

    private final MongoCollection<Document> collection;

    public MongoJobStateRepository(final MongoDatabase database) {
        this.collection = database.getCollection(JOBSTATE_COLLECTION_NAME);
    }

    @Override
    public void setValue(String jobType, String key, String value) {
        if (value != null) {
            collection.updateOne(eq(ID, jobType), set(key, value), UPSERT);
        } else {
            collection.updateOne(eq(ID, jobType), unset(key), UPSERT);
        }
    }

    @Override
    public String getValue(String jobType, String key) {
        Document first = collection.find(eq(ID, jobType)).first();
        return first != null ? first.getString(key) : null;
    }

    @Override
    public void deleteAll() {
        collection.deleteMany(new Document());
    }

    @Override
    public boolean createValue(String jobType, String key, String value) {

        Bson filter = and(
                eq(ID, jobType),
                exists(key, false));

        Bson update = set(key, value);
        try {
            Document previous = collection.findOneAndUpdate(filter, update, new FindOneAndUpdateOptions().upsert(true));
            return previous == null || previous.getString(key) == null;
        } catch (final DuplicateKeyException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return "MongoJobStateRepository";
    }
}
