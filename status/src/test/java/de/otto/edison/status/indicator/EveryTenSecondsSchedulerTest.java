package de.otto.edison.status.indicator;


import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.VersionInfo;
import de.otto.edison.status.scheduler.EveryTenSecondsScheduler;
import de.otto.edison.status.scheduler.Scheduler;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@Test
public class EveryTenSecondsSchedulerTest {

    public static final ApplicationStatus SOME_STATUS = ApplicationStatus.applicationStatus("someName", "someHost", VersionInfo.versionInfo("someVersion", "someCommit"), emptyList());
    public static final ApplicationStatus SOME_OTHER_STATUS = ApplicationStatus.applicationStatus("someOtherName", "someHost", VersionInfo.versionInfo("someVersion", "someCommit"), emptyList());

    @Test
    public void shouldDelegateStatusAggregation() throws Exception {
        ApplicationStatusAggregator statusAggregator = mock(ApplicationStatusAggregator.class);
        when(statusAggregator.aggregatedStatus()).thenReturn(SOME_STATUS);

        Scheduler scheduler = new EveryTenSecondsScheduler(statusAggregator);
        scheduler.update();
        assertThat(statusAggregator.aggregatedStatus(), is(SOME_STATUS));
    }

    @Test
    public void shouldUpdateStatus() throws Exception {
        ApplicationStatusAggregator statusAggregator = mock(ApplicationStatusAggregator.class);
        when(statusAggregator.aggregatedStatus())
                .thenReturn(SOME_STATUS)
                .thenReturn(SOME_OTHER_STATUS);

        Scheduler scheduler = new EveryTenSecondsScheduler(statusAggregator);
        // when
        scheduler.update();
        // then
        assertThat(statusAggregator.aggregatedStatus(), is(SOME_STATUS));
        // when
        scheduler.update();
        // then
        assertThat(statusAggregator.aggregatedStatus(), is(SOME_OTHER_STATUS));
    }
}