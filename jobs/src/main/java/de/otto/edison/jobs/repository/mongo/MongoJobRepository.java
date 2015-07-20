package de.otto.edison.jobs.repository.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobInfo.JobStatus;
import de.otto.edison.jobs.domain.JobMessage;
import de.otto.edison.jobs.domain.Level;
import de.otto.edison.jobs.monitor.JobMonitor;
import de.otto.edison.jobs.repository.JobRepository;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;

import static de.otto.edison.jobs.domain.JobInfo.newJobInfo;
import static de.otto.edison.jobs.domain.JobMessage.jobMessage;
import static de.otto.edison.jobs.repository.mongo.DateTimeConverters.toDate;
import static de.otto.edison.jobs.repository.mongo.DateTimeConverters.toOffsetDateTime;
import static de.otto.edison.jobs.repository.mongo.JobStructure.*;
import static java.time.Clock.systemDefaultZone;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.Date.from;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class MongoJobRepository implements JobRepository {

    private static final Logger LOG = LoggerFactory.getLogger(MongoJobRepository.class);
    private static final int DESCENDING = -1;

    @Autowired
    private MongoDatabase database;
    @Autowired
    private JobMonitor monitor;
    private final Clock clock;

    public MongoJobRepository() {
        this.clock = systemDefaultZone();
    }

    public MongoJobRepository(final MongoDatabase database, final JobMonitor jobMonitor, final Clock clock) {
        this.database = database;
        this.monitor = jobMonitor;
        this.clock = clock;
    }

    public List<JobInfo> findAll() {
        return jobs()
                .find()
                .map(this::toJobInfo)
                .into(new ArrayList<>());
    }

    @Override
    public Optional<JobInfo> findBy(final URI uri) {
        return ofNullable(jobs()
                .find(byId(uri))
                .map(this::toJobInfo)
                .first());
    }

    @Override
    public void createOrUpdate(final JobInfo job) {
        final Document document = toDocument(job);
        final Document existing = jobs().find(byId(job.getJobUri())).first();
        if (existing != null) {
            jobs().replaceOne(byId(job.getJobUri()), document);
        } else {
            jobs().insertOne(document);
        }
    }

    @Override
    public void removeIfStopped(final URI uri) {
        findBy(uri).ifPresent(jobInfo -> {
            if (jobInfo.isStopped()) {
                jobs().deleteOne(byId(uri));
            }
        });
    }

    @Override
    public List<JobInfo> findLatest(final int maxCount) {
        return jobs()
                .find()
                .limit(maxCount)
                .sort(orderByStarted(DESCENDING))
                .map(this::toJobInfo)
                .into(new ArrayList<>());
    }

    @Override
    public List<JobInfo> findLatestBy(final String type, final int maxCount) {
        return jobs()
                .find(byType(type))
                .limit(maxCount)
                .sort(orderByStarted(DESCENDING))
                .map(this::toJobInfo)
                .into(new ArrayList<>());
    }

    @Override
    public List<JobInfo> findByType(final String type) {
        return jobs()
                .find(byType(type))
                .sort(orderByStarted(DESCENDING))
                .map(this::toJobInfo)
                .into(new ArrayList<>());
    }

    @Override
    public List<JobInfo> findRunningWithoutUpdateSince(final OffsetDateTime timeOffset) {
        return jobs()
                .find(new Document()
                        .append(STOPPED.key(), singletonMap("$exists", false))
                        .append(LAST_UPDATED.key(), singletonMap("$lt", from(timeOffset.toInstant()))))
                .map(this::toJobInfo)
                .into(new ArrayList<>());
    }

    @Override
    public Optional<JobInfo> findRunningJobByType(final String jobType) {
        return ofNullable(jobs()
                .find(new Document()
                        .append(STOPPED.key(), singletonMap("$exists", false))
                        .append(JOB_TYPE.key(), jobType))
                .limit(1)
                .map(this::toJobInfo)
                .first());
    }

    @Override
    public long size() {
        return jobs().count();
    }

    public void clear() {
        jobs().deleteMany(matchAll());
    }

    private MongoCollection<Document> jobs() {
        return database.getCollection("jobs");
    }

    private Document toDocument(final JobInfo job) {
        final Document document = new Document()
                .append(ID.key(), job.getJobUri().toString())
                .append(JOB_TYPE.key(), job.getJobType())
                .append(STARTED.key(), toDate(job.getStarted()))
                .append(LAST_UPDATED.key(), toDate(job.getLastUpdated()))
                .append(MESSAGES.key(), job.getMessages().stream()
                        .map(jm -> new LinkedHashMap<String, Object>() {{
                            put(MSG_LEVEL.key(), jm.getLevel().name());
                            put(MSG_TS.key(), toDate(jm.getTimestamp()));
                            put(MSG_TEXT.key(), jm.getMessage());
                        }})
                        .collect(toList()))
                .append(STATE.key(), job.getState())
                .append(STATUS.key(), job.getStatus().name());
        if (job.isStopped()) {
            document.append(STOPPED.key(), toDate(job.getStopped().get()));
        }
        return document;
    }

    private JobInfo toJobInfo(final Document document) {
        return newJobInfo(
                URI.create(document.getString(ID.key())),
                document.getString(JOB_TYPE.key()),
                toOffsetDateTime(document.getDate(STARTED.key())),
                toOffsetDateTime(document.getDate(LAST_UPDATED.key())),
                ofNullable(toOffsetDateTime(document.getDate(STOPPED.key()))),
                JobStatus.valueOf(document.getString(STATUS.key())),
                getMessagesFrom(document),
                monitor,
                clock);
    }

    @SuppressWarnings("unchecked")
    private List<JobMessage> getMessagesFrom(final Document document) {
        List<Document> messages = (List<Document>) document.get(MESSAGES.key());
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
                Level.valueOf(document.get(MSG_LEVEL.key()).toString()),
                document.get(MSG_TEXT.key()).toString(),
                toOffsetDateTime(document.getDate(MSG_TS.key()))
        );
    }

    private Document byId(final URI uri) {
        return new Document(ID.key(), uri.toString());
    }

    private Document byType(final String type) {
        return new Document(JOB_TYPE.key(), type);
    }

    private Document matchAll() {
        return new Document();
    }

    private Document orderByStarted(final int order) {
        return new Document(STARTED.key(), order);
    }

}
