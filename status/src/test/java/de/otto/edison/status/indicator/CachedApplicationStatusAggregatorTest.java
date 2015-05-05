package de.otto.edison.status.indicator;


import de.otto.edison.status.domain.Status;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.domain.VersionInfo;
import org.testng.annotations.Test;

import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@Test
public class CachedApplicationStatusAggregatorTest {

    public static final StatusDetail SOME_DETAIL = statusDetail("someName", Status.OK, "a message");
    public static final StatusDetail SOME_OTHER_DETAIL = statusDetail("someOtherName", Status.ERROR, "a message");

    @Test
    public void shouldCacheStatus() throws Exception {
        // given
        final StatusDetailIndicator mockIndicator = someStatusDetailIndicator(SOME_DETAIL);
        final ApplicationStatusAggregator statusAggregator = new CachedApplicationStatusAggregator(
                "Test", mock(VersionInfo.class), singletonList(mockIndicator)
        );
        statusAggregator.update();
        // when
        statusAggregator.aggregatedStatus();
        statusAggregator.aggregatedStatus();
        statusAggregator.aggregatedStatus();
        // then
        verify(mockIndicator,times(1)).statusDetail();
    }

    @Test
    public void shouldAggregateStatusDetails() throws Exception {
        // given
        final ApplicationStatusAggregator statusAggregator = new CachedApplicationStatusAggregator(
                "Test",
                mock(VersionInfo.class),
                asList(
                        someStatusDetailIndicator(SOME_DETAIL),
                        someStatusDetailIndicator(SOME_OTHER_DETAIL)
                )
        );
        statusAggregator.update();
        // when
        statusAggregator.aggregatedStatus();
        // then
        assertThat(statusAggregator.aggregatedStatus().getStatus(), is(Status.ERROR));
        assertThat(statusAggregator.aggregatedStatus().getStatusDetails().get(0), is(SOME_DETAIL));
        assertThat(statusAggregator.aggregatedStatus().getStatusDetails().get(1), is(SOME_OTHER_DETAIL));
    }

    private StatusDetailIndicator someStatusDetailIndicator(final StatusDetail statusDetail) {
        final StatusDetailIndicator mockIndicator = mock(StatusDetailIndicator.class);
        when(mockIndicator.statusDetail()).thenReturn(statusDetail);
        return mockIndicator;
    }
}