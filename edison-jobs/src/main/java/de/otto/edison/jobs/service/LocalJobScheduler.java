package de.otto.edison.jobs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(value = "edison.jobs.localScheduling.enabled", havingValue = "true")
public class LocalJobScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(LocalJobScheduler.class);

    private final List<JobRunnable> jobRunnables;
    private final JobService jobService;
    private final TaskScheduler taskScheduler;

    public LocalJobScheduler(List<JobRunnable> jobRunnables, JobService jobService, TaskScheduler taskScheduler) {
        this.jobRunnables = jobRunnables;
        this.jobService = jobService;
        this.taskScheduler = taskScheduler;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void schedule() {
        TriggerContext dummyTriggerContext = new SimpleTriggerContext();
        jobRunnables.stream()
                .map(JobRunnable::getJobDefinition)
                .filter(jobDefinition -> jobDefinition.cron().isPresent() || jobDefinition.fixedDelay().isPresent())
                .forEach(jobDefinition -> {
                    Trigger trigger;
                    if (jobDefinition.cron().isPresent()) {
                        trigger = new CronTrigger(jobDefinition.cron().get());
                    } else {
                        trigger = new PeriodicTrigger(jobDefinition.fixedDelay().get());
                    }

                    taskScheduler.schedule(() -> jobService.startAsyncJob(jobDefinition.jobType()), trigger);
                    LOG.info("Scheduled {} for {}", jobDefinition.jobType(), trigger.nextExecution(dummyTriggerContext));
                });
    }
}
