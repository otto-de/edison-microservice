package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.StatefulJobRunnable;
import de.otto.edison.jobs.eventbus.JobEventPublisher;
import de.otto.edison.jobs.repository.JobStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.manuallyTriggerableJobDefinition;

@Component
public class StatefulJob extends StatefulJobRunnable {

    @Autowired
    public StatefulJob(final JobStateRepository jobStateRepository) {
        super(jobStateRepository);
    }

    @Override
    public JobDefinition getJobDefinition() {
        return manuallyTriggerableJobDefinition(
                "ExtraValueJob",
                "ExtraValueJob",
                "A Job that stores extra values in JobState",
                0,
                Optional.empty());
    }

    @Override
    public void execute(JobEventPublisher jobEventPublisher) {

        int lastEntry = meta().getAsInt("lastEntry", 0);

        for (int i = lastEntry+1; i <= lastEntry + 10; i++) {
            jobEventPublisher.info("Processing Item " + i);
            meta().set("lastEntry", i);
        }
    }
}
