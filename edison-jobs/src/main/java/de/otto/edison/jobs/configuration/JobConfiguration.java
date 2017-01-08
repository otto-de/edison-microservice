package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.repository.JobRepository;
import de.otto.edison.jobs.repository.cleanup.KeepLastJobs;
import de.otto.edison.jobs.repository.cleanup.StopDeadJobs;
import de.otto.edison.jobs.repository.inmem.InMemJobRepository;
import de.otto.edison.jobs.service.JobDefinitionService;
import de.otto.edison.jobs.service.JobService;
import de.otto.edison.jobs.status.JobStatusCalculator;
import de.otto.edison.jobs.status.JobStatusDetailIndicator;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.indicator.CompositeStatusDetailIndicator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.stream.Collectors.toList;

@Configuration
@EnableAsync
@EnableScheduling
public class JobConfiguration {

    public static final Logger LOG = LoggerFactory.getLogger(JobConfiguration.class);

    @Value("${edison.jobs.scheduler.thread-count:10}")
    int numberOfThreads;

    @Value("${edison.jobs.cleanup.number-to-keep:100}")
    int numberOfJobsToKeep;

    @Value("${edison.jobs.cleanup.mark-dead-after:30}")
    int secondsToMarkJobsAsDead;

    @Value("${edison.jobs.status.calculator.default:warningOnLastJobFailed}")
    String defaultStatusCalculator;
    /*
    @Value("${edison.jobs.status.calculator:}")
    Map<String,String> statusCalculators = emptyMap();
    */

    @Bean
    @ConditionalOnMissingBean(ScheduledExecutorService.class)
    public ScheduledExecutorService scheduledExecutorService() {
        return newScheduledThreadPool(numberOfThreads);
    }

    @Bean
    @ConditionalOnMissingBean(JobRepository.class)
    public JobRepository jobRepository() {
        LOG.warn("===============================");
        LOG.warn("Using in-memory JobRepository");
        LOG.warn("===============================");
        return new InMemJobRepository();
    }

    @Bean
    @ConditionalOnMissingBean(KeepLastJobs.class)
    public KeepLastJobs keepLastJobsStrategy() {
        return new KeepLastJobs(numberOfJobsToKeep);
    }

    @Bean
    @ConditionalOnMissingBean(StopDeadJobs.class)
    public StopDeadJobs deadJobStrategy(final JobService jobService) {
        return new StopDeadJobs(jobService, secondsToMarkJobsAsDead);
    }

    @Bean
    public JobStatusCalculator warningOnLastJobFailed(final JobRepository jobRepository) {
        return JobStatusCalculator.warningOnLastJobFailed("warningOnLastJobFailed", jobRepository);
    }

    @Bean
    public JobStatusCalculator errorOnLastJobFailed(final JobRepository jobRepository) {
        return JobStatusCalculator.errorOnLastJobFailed("errorOnLastJobFailed", jobRepository);
    }

    @Bean
    public JobStatusCalculator errorOnLastThreeJobsFailed(final JobRepository jobRepository) {
        return JobStatusCalculator.errorOnLastNumJobsFailed("errorOnLastThreeJobsFailed", 3, jobRepository);
    }

    @Bean
    public JobStatusCalculator errorOnLastTenJobsFailed(final JobRepository jobRepository) {
        return JobStatusCalculator.errorOnLastNumJobsFailed("errorOnLastTenJobsFailed", 10, jobRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "edison.jobs.status.enabled", havingValue = "true", matchIfMissing = true)
    public StatusDetailIndicator jobStatusDetailIndicator(final JobDefinitionService service,
                                                          final List<JobStatusCalculator> calculators) {
        final List<JobDefinition> jobDefinitions = service.getJobDefinitions();

        if (jobDefinitions.isEmpty()) {
            return () -> statusDetail("Jobs", Status.OK, "No job definitions configured in application.");
        } else {
            return new CompositeStatusDetailIndicator("Jobs",
                    jobDefinitions
                            .stream()
                            .map(d -> new JobStatusDetailIndicator(d, findJobStatusCalculator(d.jobType(), calculators)))
                            .collect(toList())
            );
        }
    }

    private JobStatusCalculator findJobStatusCalculator(final String jobType,
                                                        final List<JobStatusCalculator> calculators) {
        String key = defaultStatusCalculator;
        /*
        if (statusCalculators.containsKey(jobType)) {
            key = statusCalculators.get(jobType);
        }
        */
        return calculators
                .stream()
                .filter(c -> key.equalsIgnoreCase(c.getKey()))
                .findAny()
                .orElseThrow(() -> new IllegalStateException("Unable to find JobStatusCalculator " + key));
    }

}
