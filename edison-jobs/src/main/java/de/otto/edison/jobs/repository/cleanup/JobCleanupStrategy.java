package de.otto.edison.jobs.repository.cleanup;

/**
 * A strategy used to clean up old jobs from the JobRepository.
 *
 * @author Guido Steinacker
 * @since 26.02.15
 */
public interface JobCleanupStrategy {

    void doCleanUp();

}
