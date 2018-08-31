package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import org.junit.Test;

import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CachedApplicationStatusAggregatorTest {

    public static final StatusDetail OK_DETAIL_ONE = statusDetail("one", Status.OK, "a message");
    public static final StatusDetail OK_DETAIL_TWO = statusDetail("two", Status.OK, "a message");
    public static final StatusDetail WARNING_DETAIL = statusDetail("iHaveAWarning", Status.WARNING, "a message");
    public static final StatusDetail ERROR_DETAIL = statusDetail("thatsAnError", Status.ERROR, "a message");

    @Test
    public void shouldCacheStatus() throws Exception {
        // given
        final StatusDetailIndicator mockIndicator = someStatusDetailIndicator(OK_DETAIL_ONE);
        final ApplicationStatusAggregator statusAggregator = new CachedApplicationStatusAggregator(
                mock(ApplicationStatus.class), singletonList(mockIndicator)
        );
        statusAggregator.update();
        // when
        statusAggregator.aggregatedStatus();
        statusAggregator.aggregatedStatus();
        statusAggregator.aggregatedStatus();
        // then
        verify(mockIndicator, times(1)).statusDetails();
    }

    @Test
    public void shouldAggregateStatusDetails() throws Exception {
        // given
        final ApplicationStatusAggregator statusAggregator = new CachedApplicationStatusAggregator(
                mock(ApplicationStatus.class), asList(
                        someStatusDetailIndicator(OK_DETAIL_ONE),
                        someStatusDetailIndicator(ERROR_DETAIL)
                )
        );
        statusAggregator.update();
        // when
        statusAggregator.aggregatedStatus();
        // then
        assertThat(statusAggregator.aggregatedStatus().status, is(Status.ERROR));
        assertThat(statusAggregator.aggregatedStatus().statusDetails.get(0), is(OK_DETAIL_ONE));
        assertThat(statusAggregator.aggregatedStatus().statusDetails.get(1), is(ERROR_DETAIL));
    }

    @Test
    public void shouldAggregateCompositeStatusDetails() throws Exception {
        // given
        final ApplicationStatusAggregator statusAggregator = new CachedApplicationStatusAggregator(
                mock(ApplicationStatus.class), asList(
                        someCompositeStatusDetailIndicator(OK_DETAIL_ONE, WARNING_DETAIL),
                        someStatusDetailIndicator(OK_DETAIL_TWO)
                )
        );
        statusAggregator.update();
        // when
        statusAggregator.aggregatedStatus();
        // then
        assertThat(statusAggregator.aggregatedStatus().status, is(Status.WARNING));
        assertThat(statusAggregator.aggregatedStatus().statusDetails.get(0), is(OK_DETAIL_ONE));
        assertThat(statusAggregator.aggregatedStatus().statusDetails.get(1), is(WARNING_DETAIL));
        assertThat(statusAggregator.aggregatedStatus().statusDetails.get(2), is(OK_DETAIL_TWO));
    }

    private StatusDetailIndicator someCompositeStatusDetailIndicator(final StatusDetail... statusDetails) {
        final StatusDetailIndicator mockIndicator = mock(StatusDetailIndicator.class);
        when(mockIndicator.statusDetails()).thenReturn(asList(statusDetails));
        return mockIndicator;
    }

    private StatusDetailIndicator someStatusDetailIndicator(final StatusDetail statusDetail) {
        final StatusDetailIndicator mockIndicator = mock(StatusDetailIndicator.class);
        when(mockIndicator.statusDetails()).thenReturn(asList(statusDetail));
        return mockIndicator;
    }
}