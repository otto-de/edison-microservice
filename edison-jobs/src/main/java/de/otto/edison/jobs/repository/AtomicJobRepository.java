package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.service.JobRunLockProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Proxy;

//@Service
public class AtomicJobRepository {

    public static final String REPOSITORY_LOCK = "REPOSITORY_LOCK";

    public interface RepositoryRunnable<ReturnType> {
        ReturnType exec(JobRepository repository);
    }

    private JobRepository repository;
    private JobRunLockProvider runLockProvider;

    @Autowired
    public AtomicJobRepository(JobRepository repository, JobRunLockProvider runLockProvider) {
        this.repository = repository;
        this.runLockProvider = runLockProvider;
    }


    public <T> T atomicRepositoryOperation(RepositoryRunnable<T> runnable) {
        lockRepository();
        try {
            return runnable.exec(repository);
        }
        finally {
            releaseRepository();
        }
    }

    void lockRepository() {
        //ADD RETRY
        boolean b = runLockProvider.acquireRunLockForJobType(REPOSITORY_LOCK);
    }

    void releaseRepository() {
        runLockProvider.releaseRunLockForJobType(REPOSITORY_LOCK);
    }

//    @Scheduled -- every 20 - 60 seconds?!
    public void cleanupLock(){
        //TODO cleanup old locks scheduled every 20 seconds (or so)
    }
}
