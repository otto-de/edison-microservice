package de.otto.edison.togglz.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class S3TogglzRepositoryTest {

    private S3TogglzRepository s3TogglzRepository;

    @Mock
    private FeatureStateConverter featureStateConverter;

    @Mock
    private Feature feature;

    @Mock
    private FeatureState featureState;

    @BeforeEach
    public void setUp() {
        openMocks(this);
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
        verifyNoMoreInteractions(featureStateConverter);
    }

    @Test
    void shouldThrowExceptionWhenFeatureIsNotPresent() {
        s3TogglzRepository.getFeatureState(feature);
        assertThrows(IllegalArgumentException.class, () -> s3TogglzRepository.prefetchFeatureStates());
    }
}
