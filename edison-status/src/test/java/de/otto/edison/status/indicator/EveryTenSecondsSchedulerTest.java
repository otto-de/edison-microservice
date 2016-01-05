package de.otto.edison.status.indicator;


import de.otto.edison.status.domain.*;
import de.otto.edison.status.scheduler.EveryTenSecondsScheduler;
import de.otto.edison.status.scheduler.Scheduler;
import org.testng.annotations.Test;

import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test
public class EveryTenSecondsSchedulerTest {

    public static final ApplicationStatus SOME_STATUS = applicationStatus(mock(ApplicationInfo.class), mock(SystemInfo.class), mock(VersionInfo.class), singletonList(statusDetail("test", Status.OK, "everything is fine")));
    public static final ApplicationStatus SOME_OTHER_STATUS = applicationStatus(mock(ApplicationInfo.class), mock(SystemInfo.class), mock(VersionInfo.class), singletonList(statusDetail("test", Status.ERROR, "some error")));

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