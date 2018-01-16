package de.otto.edison.example.jobs;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.MetaJobRunnable;
import de.otto.edison.jobs.repository.JobMetaRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.manuallyTriggerableJobDefinition;
import static de.otto.edison.jobs.domain.JobMarker.JOB;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class ExampleMetaJob extends MetaJobRunnable {

    private static final Logger LOG = getLogger(ExampleMetaJob.class);
    public static final String JOB_TYPE = "ExampleMetaJob";

    @Autowired
    public ExampleMetaJob(final JobMetaRepository metaRepository) {
        super(JOB_TYPE, metaRepository);
    }

    @Override
    public JobDefinition getJobDefinition() {
        return manuallyTriggerableJobDefinition(
                JOB_TYPE,
                "Some stateful Job",
                "A Job that stores some meta data",
                0,
                Optional.empty());
    }

    @Override
    public boolean execute() {
        int lastEntry = getMetaAsInt("lastEntry", 0);

        for (int i = lastEntry+1; i <= lastEntry + 10; i++) {
            LOG.info(JOB, "Processing Item {}", i);
            setMeta("lastEntry", i);
        }
        return true;
    }
}
