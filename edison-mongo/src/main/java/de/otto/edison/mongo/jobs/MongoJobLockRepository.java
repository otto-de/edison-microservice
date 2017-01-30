package de.otto.edison.mongo.jobs;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.RunningJobs;
import de.otto.edison.jobs.repository.JobBlockedException;
import de.otto.edison.jobs.repository.JobLockRepository;
import de.otto.edison.jobs.service.JobMutexGroups;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

import static com.mongodb.ReadPreference.primaryPreferred;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class MongoJobLockRepository implements JobLockRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MongoJobLockRepository.class);

    private static final String JOBS_META_DATA_COLLECTION_NAME = "jobmetadata";
    private static final String RUNNING_JOBS_DOCUMENT = "RUNNING_JOBS";
    private static final String DISABLED_JOBS_DOCUMENT = "DISABLED_JOBS";
    private static final String ID = "_id";


    private final JobMutexGroups mutexGroups;
    private final MongoCollection<Document> collection;

    public MongoJobLockRepository(final MongoDatabase database, final JobMutexGroups mutexGroups) {
        this.collection = database.getCollection(JOBS_META_DATA_COLLECTION_NAME).withReadPreference(primaryPreferred());
        this.mutexGroups = mutexGroups;
    }

    @PostConstruct
    public void init() {
        if (collection.count(eq(ID, RUNNING_JOBS_DOCUMENT)) == 0) {
            collection.insertOne(new Document(ID, RUNNING_JOBS_DOCUMENT));
        }
        if (collection.count(eq(ID, DISABLED_JOBS_DOCUMENT)) == 0) {
            collection.insertOne(new Document(ID, DISABLED_JOBS_DOCUMENT));
        }
    }

    @Override
    public void markJobAsRunningIfPossible(final JobInfo jobInfo) throws JobBlockedException {
        Bson disabledJobsFilter = and(eq(ID, DISABLED_JOBS_DOCUMENT), exists(jobInfo.getJobType()));

        if (collection.find(disabledJobsFilter).first() != null) {
            throw new JobBlockedException("Disabled");
        }

        Bson query = and(
                eq(ID,
                        RUNNING_JOBS_DOCUMENT),
                and(
                        mutexGroups.mutexJobTypesFor(jobInfo.getJobType()).stream()
                                .map(type -> Filters.not(Filters.exists(type)))
                                .collect(toList())
                )
        );

        Document updatedRunningJobsDocument = collection.findOneAndUpdate(query, set(jobInfo.getJobType(), jobInfo.getJobId()));
        if (updatedRunningJobsDocument == null) {
            throw new JobBlockedException("job blocked by other '" + jobInfo.getJobType() + "' job");
        }
    }

    @Override
    public void clearRunningMark(final String jobType) {
        final Bson query = eq(ID, RUNNING_JOBS_DOCUMENT);
        final Document updateResult = collection.findOneAndUpdate(query, unset(jobType));
        if (updateResult == null) {
            LOG.warn("Could not clear running Mark for Job {}", jobType);
        }
    }

    @Override
    public RunningJobs runningJobs() {
        final Document runningJobsDocument = collection.find(eq(ID, RUNNING_JOBS_DOCUMENT))
                .first();
        if (runningJobsDocument == null) {
            return new RunningJobs(emptyList());
        }

        final List<RunningJobs.RunningJob> runningJobs = runningJobsDocument.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(ID))
                .map(entry -> new RunningJobs.RunningJob(entry.getValue().toString(), entry.getKey()))
                .collect(Collectors.toList());

        return new RunningJobs(runningJobs);
    }

    @Override
    public void disableJobType(final String jobType) {
        collection.findOneAndUpdate(
                eq(ID, DISABLED_JOBS_DOCUMENT),
                set(jobType, "disabled"),
                new FindOneAndUpdateOptions().upsert(true)
        );
    }

    @Override
    public void enableJobType(final String jobType) {
        collection.findOneAndUpdate(
                eq(ID, DISABLED_JOBS_DOCUMENT),
                unset(jobType),
                new FindOneAndUpdateOptions().upsert(true)
        );
    }

    @Override
    public List<String> findDisabledJobTypes() {
        Document disabledJobsDocument = collection.find(eq(ID, DISABLED_JOBS_DOCUMENT)).first();
        return disabledJobsDocument.keySet().stream()
                .filter(k -> !k.equals(ID))
                .collect(toList());
    }

    @Override
    public void deleteAll() {
        collection.deleteMany(new BasicDBObject());
        init();
    }

    @Override
    public long size() {
        return collection.count();
    }

}
