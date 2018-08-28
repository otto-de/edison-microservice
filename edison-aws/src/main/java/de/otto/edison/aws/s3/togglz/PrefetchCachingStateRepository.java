package de.otto.edison.aws.s3.togglz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Togglz state repository, that fetches the s3 state async
 *  to avoid s3 access while asking for togglz state.
 */
public class PrefetchCachingStateRepository implements StateRepository {

    private final static Logger LOG = LoggerFactory.getLogger(PrefetchCachingStateRepository.class);
    private final static int SCHEDULE_RATE = 30000;

    private final Map<Feature, CacheEntry> cache = new ConcurrentHashMap<>();
    private final StateRepository delegate;

    public PrefetchCachingStateRepository(final StateRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public FeatureState getFeatureState(final Feature feature) {

        final CacheEntry cachedEntry = cache.get(feature);

        if (cachedEntry != null) {
            return cachedEntry.getState();
        }

        // no cache hit - refresh state from delegate
        final FeatureState featureState = delegate.getFeatureState(feature);
        cache.put(feature, new CacheEntry(featureState));

        return featureState;
    }

    @Override
    public void setFeatureState(final FeatureState featureState) {
        delegate.setFeatureState(featureState);
        cache.put(featureState.getFeature(), new CacheEntry(featureState));
    }

    @Scheduled(initialDelay = 0, fixedRate = SCHEDULE_RATE)
    private void prefetchFeatureStates() {
        if (cache.size() == 0) {
            LOG.debug("Initialize state for features");
            initializeFeatureStates();
        } else {
            LOG.debug("Refreshing state for features");
            cache.replaceAll((feature, cacheEntry) -> new CacheEntry(delegate.getFeatureState(feature)));
        }
    }

    private void initializeFeatureStates() {
        try {
            FeatureContext.getFeatureManager().getFeatures().forEach(this::getFeatureState);
        } catch (final Exception ex) {
            LOG.error("Unable to Initialize feature states", ex);
        }
    }

    /**
     * This class represents a cached repository lookup
     */
    private static class CacheEntry {

        private final FeatureState state;

        public CacheEntry(final FeatureState state) {
            this.state = state;
        }

        public FeatureState getState() {
            return state;
        }

    }
}
