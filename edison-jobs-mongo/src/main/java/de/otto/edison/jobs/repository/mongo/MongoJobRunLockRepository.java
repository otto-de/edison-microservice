package de.otto.edison.jobs.repository.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import de.otto.edison.mongo.AbstractMongoRepository;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import static de.otto.edison.jobs.repository.mongo.DateTimeConverters.toDate;
import static de.otto.edison.jobs.repository.mongo.DateTimeConverters.toOffsetDateTime;
import static de.otto.edison.jobs.repository.mongo.JobRunLockStructure.CREATED;

@Repository(value = "jobRunLockRepository")
public class MongoJobRunLockRepository extends AbstractMongoRepository<String, JobRunLock>  {

    private static final String COLLECTION_NAME = "jobRunLocks";

    private final MongoCollection<Document> collection;

    @Autowired
    public MongoJobRunLockRepository(final MongoDatabase database) {
        this.collection = database.getCollection(COLLECTION_NAME);
    }

    @Override
    protected void ensureIndexes() {

    }

    @Override
    protected MongoCollection<Document> collection() {
        return collection;
    }

    @Override
    protected String keyOf(JobRunLock value) {
        return value.getId();
    }

    @Override
    protected Document encode(JobRunLock value) {
        return new Document()
                .append(JobRunLockStructure.ID.key(), value.getId())
                .append(CREATED.key(), toDate(value.getCreated()));
    }

    @Override
    protected JobRunLock decode(Document document) {
        return new JobRunLock(
                document.getString(JobRunLockStructure.ID.key()),
                toOffsetDateTime(document.getDate(CREATED.key())));
    }


}
