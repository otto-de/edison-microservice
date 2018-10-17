package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.SystemInfo;
import de.otto.edison.status.domain.TeamInfo;
import de.otto.edison.status.domain.VersionInfo;
import de.otto.edison.status.scheduler.EveryTenSecondsScheduler;
import de.otto.edison.status.scheduler.Scheduler;
import org.junit.jupiter.api.Test;

import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EveryTenSecondsSchedulerTest {

    public static final ApplicationStatus SOME_STATUS = applicationStatus(mock(ApplicationInfo.class), null, mock(SystemInfo.class), mock(VersionInfo.class), mock(TeamInfo.class), singletonList(statusDetail("test", Status.OK, "everything is fine")));
    public static final ApplicationStatus SOME_OTHER_STATUS = applicationStatus(mock(ApplicationInfo.class), null, mock(SystemInfo.class), mock(VersionInfo.class), mock(TeamInfo.class), singletonList(statusDetail("test", Status.ERROR, "some error")));

    @Test
    public void shouldDelegateStatusAggregation() throws Exception {
        final ApplicationStatusAggregator statusAggregator = mock(ApplicationStatusAggregator.class);
        when(statusAggregator.aggregatedStatus()).thenReturn(SOME_STATUS);

        final Scheduler scheduler = new EveryTenSecondsScheduler(statusAggregator);
        scheduler.update();
        assertThat(statusAggregator.aggregatedStatus(), is(SOME_STATUS));
    }

    @Test
    public void shouldUpdateStatus() throws Exception {
        final ApplicationStatusAggregator statusAggregator = mock(ApplicationStatusAggregator.class);
        when(statusAggregator.aggregatedStatus())
                .thenReturn(SOME_STATUS)
                .thenReturn(SOME_OTHER_STATUS);

        final Scheduler scheduler = new EveryTenSecondsScheduler(statusAggregator);
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