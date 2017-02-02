package de.otto.edison.mongo.jobs;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import de.otto.edison.jobs.repository.JobStateRepository;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

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
        collection.updateOne(eq(ID, jobType), Updates.set(key, value), UPSERT);
    }

    @Override
    public String getValue(String jobType, String key) {
        Document first = collection.find(eq(ID, jobType)).first();
        return first != null ? first.getString(key) : null;
    }

    @Override
    public String toString() {
        return "MongoJobStateRepository";
    }
}
