package de.otto.edison.status.indicator;


import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.domain.VersionInfo;
import org.testng.annotations.Test;

import java.util.Collections;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@Test
public class CachingApplicationStatusAggregatorTest {

    public static final ApplicationStatus SOME_STATUS = ApplicationStatus.applicationStatus("someName", "someHost", VersionInfo.versionInfo("someVersion", "someCommit"), emptyList());
    public static final ApplicationStatus SOME_OTHER_STATUS = ApplicationStatus.applicationStatus("someOtherName", "someHost", VersionInfo.versionInfo("someVersion", "someCommit"), emptyList());

    @Test
    public void shouldDelegateStatusAggregation() throws Exception {
        ApplicationStatusAggregator statusAggregator = mock(ApplicationStatusAggregator.class);
        when(statusAggregator.aggregate()).thenReturn(SOME_STATUS);

        CachingApplicationStatusAggregator cachingApplicationStatusAggregator = new CachingApplicationStatusAggregator(statusAggregator);
        cachingApplicationStatusAggregator.update();
        assertThat(cachingApplicationStatusAggregator.aggregate(), is(SOME_STATUS));
    }

    @Test
    public void shouldUpdateStatus() throws Exception {
        ApplicationStatusAggregator statusAggregator = mock(ApplicationStatusAggregator.class);
        when(statusAggregator.aggregate())
                .thenReturn(SOME_STATUS)
                .thenReturn(SOME_OTHER_STATUS);

        CachingApplicationStatusAggregator cachingApplicationStatusAggregator = new CachingApplicationStatusAggregator(statusAggregator);

        cachingApplicationStatusAggregator.update();
        assertThat(cachingApplicationStatusAggregator.aggregate(), is(SOME_STATUS));

        cachingApplicationStatusAggregator.update();
        assertThat(cachingApplicationStatusAggregator.aggregate(), is(SOME_OTHER_STATUS));
    }

    @Test
    public void shouldCacheStatus() throws Exception {
        ApplicationStatusAggregator statusAggregator = mock(ApplicationStatusAggregator.class);
        when(statusAggregator.aggregate())
                .thenReturn(SOME_STATUS);
        CachingApplicationStatusAggregator cachingApplicationStatusAggregator = new CachingApplicationStatusAggregator(statusAggregator);
        cachingApplicationStatusAggregator.update();

        assertThat(cachingApplicationStatusAggregator.aggregate(),is(SOME_STATUS));
        assertThat(cachingApplicationStatusAggregator.aggregate(),is(SOME_STATUS));
        verify(statusAggregator,times(1)).aggregate();
    }
}