package de.otto.edison.jobs.configuration;

import de.otto.edison.jobs.definition.DefaultJobDefinition;
import de.otto.edison.jobs.definition.JobDefinition;
import de.otto.edison.jobs.service.JobDefinitionService;
import de.otto.edison.jobs.status.JobStatusCalculator;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.CompositeStatusDetailIndicator;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.junit.Test;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.fixedDelayJobDefinition;
import static de.otto.edison.status.domain.Status.OK;
import static java.time.Duration.ofSeconds;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JobConfigurationTest {

    @Test
    public void shouldIndicateOkIfNoJobDefinitionsAvailable() {
        // given
        final JobConfiguration jobConfiguration = new JobConfiguration();
        jobConfiguration.defaultStatusCalculator = "warningOnLastJobFailed";

        final JobStatusCalculator defaultCalculator = mock(JobStatusCalculator.class);
        when(defaultCalculator.getKey()).thenReturn("warningOnLastJobFailed");

        final JobDefinitionService noJobDefinitions = mock(JobDefinitionService.class);
        when(noJobDefinitions.getJobDefinitions()).thenReturn(emptyList());

        // when
        final StatusDetail statusDetail = jobConfiguration.jobStatusDetailIndicator(
                noJobDefinitions,
                singletonList(defaultCalculator)).statusDetail();
        // then
        assertThat(statusDetail.getStatus(), is(OK));
        assertThat(statusDetail.getName(), is("Jobs"));
        assertThat(statusDetail.getMessage(), is("No job definitions configured in application."));
    }

    @Test
    public void shouldConstructCompositeStatusDetailIndicator() {
        // given
        final JobConfiguration jobConfiguration = new JobConfiguration();
        jobConfiguration.defaultStatusCalculator = "warningOnLastJobFailed";

        final JobStatusCalculator defaultCalculator = mock(JobStatusCalculator.class);
        when(defaultCalculator.getKey()).thenReturn("warningOnLastJobFailed");

        // when
        final StatusDetailIndicator indicator = jobConfiguration.jobStatusDetailIndicator(
                someJobDefinitionService(),
                singletonList(defaultCalculator)
        );
        // then
        assertThat(indicator, is(instanceOf(CompositeStatusDetailIndicator.class)));
    }

    @Test
    public void shouldUseDefaultJobStatusDetailIndicator() {
        // given
        final JobConfiguration jobConfiguration = new JobConfiguration();
        jobConfiguration.defaultStatusCalculator = "warningOnLastJobFailed";

        final JobStatusCalculator defaultCalculator = mock(JobStatusCalculator.class);
        when(defaultCalculator.getKey()).thenReturn("warningOnLastJobFailed");

        // when
        jobConfiguration.jobStatusDetailIndicator(
                someJobDefinitionService(), singletonList(defaultCalculator)).statusDetail();

        // then
        verify(defaultCalculator).statusDetail(any(JobDefinition.class));
    }

    /*
    @Test
    public void shouldUseConfiguredJobStatusDetailIndicator() {
        // given
        final JobConfiguration jobConfiguration = new JobConfiguration();
        jobConfiguration.defaultStatusCalculator = "warningOnLastJobFailed";
        jobConfiguration.statusCalculators = new HashMap<String, String>() {{
                put("test", "errorOnLastJobFailed");
        }};

        final JobStatusCalculator defaultCalculator = mock(JobStatusCalculator.class);
        when(defaultCalculator.getKey()).thenReturn("warningOnLastJobFailed");

        final JobStatusCalculator testCalculator = mock(JobStatusCalculator.class);
        when(testCalculator.getKey()).thenReturn("errorOnLastJobFailed");

        // when
        jobConfiguration.jobStatusDetailIndicator(
                someJobDefinitionService(), Arrays.asList(defaultCalculator, testCalculator)).statusDetail();

        // then
        verify(testCalculator).statusDetail(any(JobDefinition.class));
    }
    */

    private JobDefinitionService someJobDefinitionService() {
        final JobDefinitionService jobDefinitionService = mock(JobDefinitionService.class);
        when(jobDefinitionService.getJobDefinitions()).thenReturn(singletonList(someJobDefinition()));
        return jobDefinitionService;
    }

    private DefaultJobDefinition someJobDefinition() {
        return fixedDelayJobDefinition(
                "test",
                "test",
                "",
                ofSeconds(10),
                0,
                of(ofSeconds(10))
        );

    }
}