package de.otto.edison.cachestatistics;

import de.otto.edison.annotations.Beta;

import java.util.List;

@Beta
public interface CacheStatisticsProvider {

    List<CacheStatistics> getStats();
}
