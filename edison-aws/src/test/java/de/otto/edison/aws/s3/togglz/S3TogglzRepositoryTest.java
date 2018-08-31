package de.otto.edison.aws.s3.togglz;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class S3TogglzRepositoryTest {

    private S3TogglzRepository s3TogglzRepository;

    @Mock
    private FeatureStateConverter featureStateConverter;

    @Mock
    private Feature feature;

    @Mock
    private FeatureState featureState;

    @Before
    public void setUp() {
        initMocks(this);
        s3TogglzRepository = new S3TogglzRepository(featureStateConverter);
        when(feature.name()).thenReturn("someToggleName");
    }

    @Test
    public void shouldFetchInitialTogglzStatefromDelegateAndServeSubsequentRequestsFromCache() {
        // given
        when(featureStateConverter.retrieveFeatureStateFromS3(feature)).thenReturn(featureState);

        // when
        s3TogglzRepository.getFeatureState(feature);
        s3TogglzRepository.getFeatureState(feature);
        s3TogglzRepository.getFeatureState(feature);

        // then
        verify(featureStateConverter, times(1)).retrieveFeatureStateFromS3(feature);
    }

    @Test
    public void shouldSetFeatureStateAndPutItIntoCache() {
        // given
        when(featureState.getFeature()).thenReturn(feature);
        // when
        s3TogglzRepository.setFeatureState(featureState);
        verify(featureStateConverter, times(1)).persistFeatureStateToS3(featureState);

        final FeatureState featureStateFromCache = s3TogglzRepository.getFeatureState(feature);
        assertThat(featureStateFromCache, is(featureState));
        verifyZeroInteractions(featureStateConverter);
    }
}