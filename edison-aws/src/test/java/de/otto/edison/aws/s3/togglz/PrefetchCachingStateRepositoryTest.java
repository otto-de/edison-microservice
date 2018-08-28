package de.otto.edison.aws.s3.togglz;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@Ignore
public class PrefetchCachingStateRepositoryTest {

    private PrefetchCachingStateRepository prefetchCachingStateRepository;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private Feature feature;

    @Mock
    private FeatureState featureState;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        prefetchCachingStateRepository = new PrefetchCachingStateRepository(stateRepository);
        when(feature.name()).thenReturn("someToggleName");
    }

    @Test
    public void shouldFetchInitialTogglzStatefromDelegateAndServeSubsequentRequestsFromCache() {
        // given
        when(stateRepository.getFeatureState(feature)).thenReturn(featureState);

        // when
        prefetchCachingStateRepository.getFeatureState(feature);
        prefetchCachingStateRepository.getFeatureState(feature);
        prefetchCachingStateRepository.getFeatureState(feature);

        // then
        verify(stateRepository, times(1)).getFeatureState(feature);
    }

    @Test
    public void shouldSetFeatureStateAndPutItIntoCache() {
        // given
        when(featureState.getFeature()).thenReturn(feature);
        // when
        prefetchCachingStateRepository.setFeatureState(featureState);
        verify(stateRepository, times(1)).setFeatureState(featureState);

        FeatureState featureStateFromCache = prefetchCachingStateRepository.getFeatureState(feature);
        assertThat(featureStateFromCache, is(featureState));
        verifyZeroInteractions(stateRepository);
    }
}