package de.otto.edison.jobs.repository;

import java.util.List;

import static java.util.Arrays.asList;

public class AggregateCleanupStrategy implements JobCleanupStrategy {
    private final List<JobCleanupStrategy> jobCleanupStrategies;

    public AggregateCleanupStrategy(JobCleanupStrategy... jobCleanupStrategies) {
        this.jobCleanupStrategies = asList(jobCleanupStrategies);
    }

    @Override
    public void doCleanUp(JobRepository repository) {
        this.jobCleanupStrategies.stream().forEach(strategy -> strategy.doCleanUp(repository));
    }
}
