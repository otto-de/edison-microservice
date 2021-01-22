package de.otto.edison.jobs.repository.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.PushOptions;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.mongo.AbstractMongoRepository;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.bson.Document;
import org.bson.RawBsonDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.mongodb.ReadPreference.primaryPreferred;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Updates.*;
import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static java.time.Clock.systemDefaultZone;
import static java.time.OffsetDateTime.ofInstant;
import static java.time.ZoneId.systemDefault;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.Date.from;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class MongoJobRepository extends AbstractMongoRepository<String, JobInfo> implements JobRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MongoJobRepository.class);

    private static final int DESCENDING = -1;
    private static final String NO_LOG_MESSAGE_FOUND = "No log message found";
    public static final String ID = "_id";

    private final static int MAX_DOCUMENT_SIZE = 16777216;

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
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
                .first().getString(JobStructure.STATUS.key()));
    }

    @Override
    public void removeIfStopped(final String id) {
        findOne(id).ifPresent(jobInfo -> {
            if (jobInfo.isStopped()) {
                collectionWithWriteTimeout(mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS).deleteOne(eq(ID, id));
            }
        });
    }

    @Override
    public void appendMessage(final String jobId, final JobMessage jobMessage) {
        collectionWithWriteTimeout(mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS).updateOne(eq(ID, jobId), combine(push(JobStructure.MESSAGES.key(), encodeJobMessage(jobMessage)), set(JobStructure.LAST_UPDATED.key(), Date.from(jobMessage.getTimestamp().toInstant()))));
    }

    @Override
    public void setJobStatus(final String jobId, final JobStatus jobStatus) {
        collectionWithWriteTimeout(mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS).updateOne(eq(ID, jobId), set(JobStructure.STATUS.key(), jobStatus.name()));
    }

    @Override
    public void setLastUpdate(final String jobId, final OffsetDateTime lastUpdate) {
        collectionWithWriteTimeout(mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS).updateOne(eq(ID, jobId), set(JobStructure.LAST_UPDATED.key(), Date.from(lastUpdate.toInstant())));
    }

    @Override
    public List<JobInfo> findLatest(final int maxCount) {
        return collection()
                .find()
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
                .sort(orderByStarted(DESCENDING))
                .limit(maxCount)
                .map(this::decode)
                .into(new ArrayList<>());
    }

    @Override
    public List<JobInfo> findLatestJobsDistinct() {
        final List<String> allJobIds = findAllJobIdsDistinct();
        return collection()
                .find(in(ID, allJobIds))
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
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
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
                .map(doc -> doc.getString("latestJobId"))
                .into(new ArrayList<>()).stream()
                .filter(Objects::nonNull)
                .collect(toList());
    }

    @Override
    public List<JobInfo> findLatestBy(final String type, final int maxCount) {
        return collection()
                .find(byType(type))
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
                .sort(orderByStarted(DESCENDING))
                .limit(maxCount)
                .map(this::decode)
                .into(new ArrayList<>());
    }

    @Override
    public List<JobInfo> findByType(final String type) {
        return collection()
                .find(byType(type))
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
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
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
                .map(this::decode)
                .into(new ArrayList<>());
    }

    @Override
    protected final Document encode(final JobInfo job) {
        final Document document = new Document()
                .append(JobStructure.ID.key(), job.getJobId())
                .append(JobStructure.JOB_TYPE.key(), job.getJobType())
                .append(JobStructure.STARTED.key(), Date.from(job.getStarted().toInstant()))
                .append(JobStructure.LAST_UPDATED.key(), Date.from(job.getLastUpdated().toInstant()))
                .append(JobStructure.MESSAGES.key(), job.getMessages().stream()
                        .map(MongoJobRepository::encodeJobMessage)
                        .collect(toList()))
                .append(JobStructure.STATUS.key(), job.getStatus().name())
                .append(JobStructure.HOSTNAME.key(), job.getHostname());
        if (job.isStopped()) {
            document.append(JobStructure.STOPPED.key(), Date.from(job.getStopped().get().toInstant()));
        }
        return document;
    }

    private static Document encodeJobMessage(final JobMessage jm) {
        return new Document() {{
            put(JobStructure.MSG_LEVEL.key(), jm.getLevel().name());
            put(JobStructure.MSG_TS.key(), Date.from(jm.getTimestamp().toInstant()));
            put(JobStructure.MSG_TEXT.key(), jm.getMessage());
        }};
    }

    @Override
    protected final JobInfo decode(final Document document) {
        return newJobInfo(
                document.getString(JobStructure.ID.key()),
                document.getString(JobStructure.JOB_TYPE.key()),
                toOffsetDateTime(document.getDate(JobStructure.STARTED.key())),
                toOffsetDateTime(document.getDate(JobStructure.LAST_UPDATED.key())),
                ofNullable(toOffsetDateTime(document.getDate(JobStructure.STOPPED.key()))),
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
                toOffsetDateTime(document.getDate(JobStructure.MSG_TS.key()))
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
        collection().createIndex(Indexes.compoundIndex(Indexes.ascending(JobStructure.JOB_TYPE.key()), Indexes.descending(JobStructure.STARTED.key())), options);
        collection().createIndex(Indexes.ascending(JobStructure.STARTED.key()), options);
        collection().createIndex(Indexes.ascending(JobStructure.LAST_UPDATED.key(), JobStructure.STOPPED.key()), options);
    }

    private String getMessage(final Document document) {
        return document.get(JobStructure.MSG_TEXT.key()) == null ? NO_LOG_MESSAGE_FOUND : document.get(JobStructure.MSG_TEXT.key()).toString();
    }

    private Document byType(final String type) {
        return new Document(JobStructure.JOB_TYPE.key(), type);
    }

    private Document orderByStarted(final int order) {
        return new Document(JobStructure.STARTED.key(), order);
    }

    @Override
    public List<JobInfo> findAllJobInfoWithoutMessages() {
        return collection()
                .find()
                .maxTime(mongoProperties.getDefaultReadTimeout(), TimeUnit.MILLISECONDS)
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

    private OffsetDateTime toOffsetDateTime(final Date date) {
        return date == null ? null : ofInstant(date.toInstant(), systemDefault());
    }

    @Override
    public void keepJobMessagesWithinMaximumSize(String jobId) {
            final Optional<JobInfo> jobInfoOptional = this.findOne(jobId);
            if (jobInfoOptional.isPresent()) {
                JobInfo jobInfo = jobInfoOptional.get();
                RawBsonDocument rawBsonDocument = RawBsonDocument.parse(encode(jobInfo).toJson());
                int bsonSize = rawBsonDocument.getByteBuffer().remaining();
                int averageMessageSize = bsonSize / Math.max(jobInfo.getMessages().size(), 1);
                LOG.debug("Bson size of running job with jobId {} is {} bytes. Average message size is {} bytes. Total messages: {}", jobId, bsonSize, averageMessageSize, jobInfo.getMessages().size());
                //Is document taking more than 3/4 of the allowed space?
                if (bsonSize > (MAX_DOCUMENT_SIZE - (MAX_DOCUMENT_SIZE / 4))) {
                    LOG.info("Bson size of running job with jobId {} is {} bytes. The size of this job's document is growing towards MongoDBs limit for single documents, so I'll drop all messages but the last 1000.", jobId, bsonSize);
                    JobMessage jobMessage = JobMessage.jobMessage(Level.INFO, "The messages array for this job is growing towards MongoDBs limit for single documents, so I'll drop all messages but the last 1000.", OffsetDateTime.now());
                    collectionWithWriteTimeout(mongoProperties.getDefaultWriteTimeout(), TimeUnit.MILLISECONDS).updateOne(eq(ID, jobId), combine(pushEach(JobStructure.MESSAGES.key(), Collections.singletonList(encodeJobMessage(jobMessage)), new PushOptions().slice(-1000)), set(JobStructure.LAST_UPDATED.key(), Date.from(jobMessage.getTimestamp().toInstant()))));
                }
            }
    }

}
