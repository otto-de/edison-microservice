package de.otto.edison.jobs.eventbus;

import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.service.JobRunnable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;

import static de.otto.edison.jobs.eventbus.events.MessageEvent.Level.ERROR;
import static de.otto.edison.jobs.eventbus.events.StateChangeEvent.State.STOP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Test
@SpringApplicationConfiguration(classes = {EventbusConfiguration.class, EventbusTestConfiguration.class})
public class EventbusIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private InMemoryEventRubbishBin inMemoryEventRubbishBin;

    @BeforeMethod
    public void setUp() throws Exception {
        inMemoryEventRubbishBin.clear();
    }

    @Test
    public void shouldSendAndReceiveStartEvent() throws Exception {
        // when
        eventPublisher.message(createJobRunnable(), new URI("some/job"), ERROR, "some message");

        // then
        assertThat(inMemoryEventRubbishBin.getMessageEvents().get(0), is("some/job"));
    }

    @Test
    public void shouldSendAndReceiveStopEvent() throws Exception {
        // when
        eventPublisher.stateChanged(createJobRunnable(), new URI("some/stopped/job"), STOP);

        // then
        assertThat(inMemoryEventRubbishBin.getStateChangedEvents().get(0), is("some/stopped/job"));
    }

    private JobRunnable createJobRunnable() {
        return new JobRunnable() {
            @Override
            public JobDefinition getJobDefinition() {
                return null;
            }

            @Override
            public void execute(JobInfo jobInfo, EventPublisher eventPublisher) {
            }
        };
    }
}
