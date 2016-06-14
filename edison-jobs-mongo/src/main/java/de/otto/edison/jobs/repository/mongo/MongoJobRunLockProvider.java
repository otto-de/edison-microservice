package de.otto.edison.jobs.repository.mongo;

import com.mongodb.MongoWriteException;
import de.otto.edison.jobs.service.JobRunLockProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.ErrorCategory.DUPLICATE_KEY;


@Service(value = "jobRunLockProvider")
public class MongoJobRunLockProvider implements JobRunLockProvider {

    private final MongoJobRunLockRepository jobRunLockRepository;

    private Clock clock;

    @Autowired
    public MongoJobRunLockProvider(MongoJobRunLockRepository jobRunLockRepository) {
        this(jobRunLockRepository, Clock.systemDefaultZone());

    }

    public MongoJobRunLockProvider(MongoJobRunLockRepository jobRunLockRepository, Clock clock) {
        this.jobRunLockRepository = jobRunLockRepository;
        this.clock = clock;
    }

    /**
     * Creates a new running lock. If the creation fails with a duplicate key error, it means, the lock has already been acquired.
     * If any other runtime exception occures, it gets thrown and has to be handled outside.
     */
    public boolean getRunLockForJobType(String jobType) {
        try {
            jobRunLockRepository.create(new JobRunLock(jobType, OffsetDateTime.now(clock)));
        }catch (MongoWriteException e) {
            if (e.getError().getCategory().equals(DUPLICATE_KEY)) {
                return false;
            } else {
                throw e;
            }
        }
        return true;
    }


    @Override
    public boolean acquireRunLocksForJobTypes(Set<String> jobTypes) {
        //Aquire locks always in the same order to avoid getting a deadlock
        List<String> orderedJobTypes = jobTypes.stream().sorted(String.CASE_INSENSITIVE_ORDER).collect(Collectors.toList());

        List<String> obtainedLocks = new ArrayList<>();
        try {
            for(String jobType : orderedJobTypes) {
                if (getRunLockForJobType(jobType)) {
                    obtainedLocks.add(jobType);
                } else {
                    break;
                }

            }
        } finally {
            if (obtainedLocks.size() != jobTypes.size()) {
                obtainedLocks.forEach(this::removeRunLockForJobType);
            }

        }
        return obtainedLocks.size() == jobTypes.size();
    }

    @Override
    public void releaseRunLocksForJobTypes(Set<String> jobTypes) {
        //Release locks always in the opposite order as in aquiring
        List<String> orderedJobTypes = jobTypes.stream().sorted(String.CASE_INSENSITIVE_ORDER.reversed()).collect(Collectors.toList());
        orderedJobTypes.forEach(this::removeRunLockForJobType);
    }

    public void removeRunLockForJobType(String jobType) {
        jobRunLockRepository.delete(jobType);
    }
}
