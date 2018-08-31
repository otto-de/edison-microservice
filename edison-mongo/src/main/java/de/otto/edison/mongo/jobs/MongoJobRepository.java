package de.otto.edison.mongo.jobs;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.mongo.AbstractMongoRepository;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.bson.Document;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.mongodb.ReadPreference.primaryPreferred;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static java.time.Clock.systemDefaultZone;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.Date.from;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class MongoJobRepository extends AbstractMongoRepository<String, JobInfo> implements JobRepository {

    private static final int DESCENDING = -1;
    private static final String NO_LOG_MESSAGE_FOUND = "No log message found";
    public static final String ID = "_id";

    private final MongoCollection<Document> jobInfoCollection;
    private final Clock clock;

    public MongoJobRepository(final MongoDatabase mongoDatabase,
                              final String jobInfoCollectionName,
                              final MongoProperties mongoProperties) {
        super(mongoProperties);
        MongoCollection<Document> tmpCollection = mongoDatabase.getCollection(jobInfoCollectionName).withReadPreference(primaryPreferred());
        this.jobInfoCollection = tmpCollection.withWriteConcern(tmpCollection.getWriteConcern().withWTimeout(mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS));
        this.clock = systemDefaultZone();
    }

    @Override
    public JobStatus findStatus(final String jobId) {
        return JobStatus.valueOf(collection()
                .find(eq(ID, jobId))
                .projection(new Document(JobStructure.STATUS.key(), true))
                .maxTime(50, TimeUnit.MILLISECONDS)
                .first().getString(JobStructure.STATUS.key()));
    }

    @Override
    public void removeIfStopped(final String id) {
        findOne(id).ifPresent(jobInfo -> {
            if (jobInfo.isStopped()) {
                collectionWithWriteTimeout(50, TimeUnit.MILLISECONDS).deleteOne(eq(ID, id));
            }
        });
    }

    @Override
    public void appendMessage(final String jobId, final JobMessage jobMessage) {
        collectionWithWriteTimeout(250, TimeUnit.MILLISECONDS).updateOne(eq(ID, jobId), combine(push(JobStructure.MESSAGES.key(), encodeJobMessage(jobMessage)), set(JobStructure.LAST_UPDATED.key(), DateTimeConverters.toDate(jobMessage.getTimestamp()))));
    }

    @Override
    public void setJobStatus(final String jobId, final JobStatus jobStatus) {
        collectionWithWriteTimeout(250, TimeUnit.MILLISECONDS).updateOne(eq(ID, jobId), set(JobStructure.STATUS.key(), jobStatus.name()));
    }

    @Override
    public void setLastUpdate(final String jobId, final OffsetDateTime lastUpdate) {
        collectionWithWriteTimeout(250, TimeUnit.MILLISECONDS).updateOne(eq(ID, jobId), set(JobStructure.LAST_UPDATED.key(), DateTimeConverters.toDate(lastUpdate)));
    }

    @Override
    public List<JobInfo> findLatest(final int maxCount) {
        return collection()
                .find()
                .maxTime(500, TimeUnit.MILLISECONDS)
                .sort(orderByStarted(DESCENDING))
                .limit(maxCount)
                .map(this::decode)
                .into(new ArrayList<>());
    }

    @Override
    public List<JobInfo> findLatestJobsDistinct() {
        final List<String> allJobIds = findAllJobIdsDistinct();
        return collection()
                .find(Filters.in(ID, allJobIds))
                .maxTime(500, TimeUnit.MILLISECONDS)
                .map(this::decode)
                .into(new ArrayList<>());
    }

    public List<String> findAllJobIdsDistinct() {
        return collection()
                .aggregate(Arrays.asList(
                        new Document("$sort", new Document("started", -1)),
                        new Document("$group", new HashMap<String, Object>() {{
                            put("_id", "$type");
                            put("latestJobId", new Document("$first", "$_id"));
                        }})))
                .maxTime(500, TimeUnit.MILLISECONDS)
                .map(doc -> doc.getString("latestJobId"))
                .into(new ArrayList<>()).stream()
                .filter(Objects::nonNull)
                .collect(toList());
    }

    @Override
    public List<JobInfo> findLatestBy(final String type, final int maxCount) {
        return collection()
                .find(byType(type))
                .maxTime(250, TimeUnit.MILLISECONDS)
                .sort(orderByStarted(DESCENDING))
                .limit(maxCount)
                .map(this::decode)
                .into(new ArrayList<>());
    }

    @Override
    public List<JobInfo> findByType(final String type) {
        return collection()
                .find(byType(type))
                .maxTime(250, TimeUnit.MILLISECONDS)
                .sort(orderByStarted(DESCENDING))
                .map(this::decode)
                .into(new ArrayList<>());
    }

    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(final OffsetDateTime timeOffset) {
        return collection()
                .find(new Document()
                        .append(JobStructure.STOPPED.key(), singletonMap("$exists", false))
                        .append(JobStructure.LAST_UPDATED.key(), singletonMap("$lt", from(timeOffset.toInstant()))))
                .maxTime(500, TimeUnit.MILLISECONDS)
                .map(this::decode)
                .into(new ArrayList<>());
    }

    @Override
    protected final Document encode(final JobInfo job) {
        final Document document = new Document()
                .append(JobStructure.ID.key(), job.getJobId())
                .append(JobStructure.JOB_TYPE.key(), job.getJobType())
                .append(JobStructure.STARTED.key(), DateTimeConverters.toDate(job.getStarted()))
                .append(JobStructure.LAST_UPDATED.key(), DateTimeConverters.toDate(job.getLastUpdated()))
                .append(JobStructure.MESSAGES.key(), job.getMessages().stream()
                        .map(MongoJobRepository::encodeJobMessage)
                        .collect(toList()))
                .append(JobStructure.STATUS.key(), job.getStatus().name())
                .append(JobStructure.HOSTNAME.key(), job.getHostname());
        if (job.isStopped()) {
            document.append(JobStructure.STOPPED.key(), DateTimeConverters.toDate(job.getStopped().get()));
        }
        return document;
    }

    private static Document encodeJobMessage(final JobMessage jm) {
        return new Document() {{
            put(JobStructure.MSG_LEVEL.key(), jm.getLevel().name());
            put(JobStructure.MSG_TS.key(), DateTimeConverters.toDate(jm.getTimestamp()));
            put(JobStructure.MSG_TEXT.key(), jm.getMessage());
        }};
    }

    @Override
    protected final JobInfo decode(final Document document) {
        return newJobInfo(
                document.getString(JobStructure.ID.key()),
                document.getString(JobStructure.JOB_TYPE.key()),
                DateTimeConverters.toOffsetDateTime(document.getDate(JobStructure.STARTED.key())),
                DateTimeConverters.toOffsetDateTime(document.getDate(JobStructure.LAST_UPDATED.key())),
                ofNullable(DateTimeConverters.toOffsetDateTime(document.getDate(JobStructure.STOPPED.key()))),
                JobStatus.valueOf(document.getString(JobStructure.STATUS.key())),
                getMessagesFrom(document),
                clock,
                document.getString(JobStructure.HOSTNAME.key()));
    }

    @SuppressWarnings("unchecked")
    private List<JobMessage> getMessagesFrom(final Document document) {
        final List<Document> messages = (List<Document>) document.get(JobStructure.MESSAGES.key());
        if (messages != null) {
            return messages.stream()
                    .map(this::toJobMessage)
                    .collect(toList());
        } else {
            return emptyList();
        }
    }

    private JobMessage toJobMessage(final Document document) {
        return jobMessage(
                Level.valueOf(document.get(JobStructure.MSG_LEVEL.key()).toString()),
                getMessage(document),
                DateTimeConverters.toOffsetDateTime(document.getDate(JobStructure.MSG_TS.key()))
        );
    }

    @Override
    protected final String keyOf(final JobInfo value) {
        return value.getJobId();
    }

    @Override
    protected final MongoCollection<Document> collection() {
        return jobInfoCollection;
    }

    @Override
    protected final void ensureIndexes() {
        IndexOptions options = new IndexOptions().background(true);
        collection().createIndex(Indexes.ascending(JobStructure.JOB_TYPE.key()), options);
        collection().createIndex(Indexes.ascending(JobStructure.STARTED.key()), options);
        collection().createIndex(Indexes.ascending(JobStructure.LAST_UPDATED.key(), JobStructure.STOPPED.key()), options);
    }

    private String getMessage(final Document document) {
        return document.get(JobStructure.MSG_TEXT.key()) == null ? NO_LOG_MESSAGE_FOUND : document.get(JobStructure.MSG_TEXT.key()).toString();
    }

    private Document byType(final String type) {
        return new Document(JobStructure.JOB_TYPE.key(), type);
    }

    private Document byTypeAndStatus(final String type, final JobStatus status) {
        return new Document(JobStructure.JOB_TYPE.key(), type).append(JobStructure.STATUS.key(), status.name());
    }

    private Document orderByStarted(final int order) {
        return new Document(JobStructure.STARTED.key(), order);
    }

    @Override
    public List<JobInfo> findAllJobInfoWithoutMessages() {
        return collection()
                .find()
                .maxTime(500, TimeUnit.MILLISECONDS)
                .projection(new Document(getJobInfoWithoutMessagesProjection()))
                .map(this::decode)
                .into(new ArrayList<>());
    }

    private Map<String, Object> getJobInfoWithoutMessagesProjection() {
        final Map<String, Object> projection = new HashMap<>();
        projection.put(JobStructure.ID.key(), true);
        projection.put(JobStructure.JOB_TYPE.key(), true);
        projection.put(JobStructure.STARTED.key(), true);
        projection.put(JobStructure.LAST_UPDATED.key(), true);
        projection.put(JobStructure.STOPPED.key(), true);
        projection.put(JobStructure.STATUS.key(), true);
        projection.put(JobStructure.HOSTNAME.key(), true);
        return projection;

    }

}
