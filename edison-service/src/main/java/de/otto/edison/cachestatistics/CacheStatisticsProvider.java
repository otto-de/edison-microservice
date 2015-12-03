package de.otto.edison.cachestatistics;

import java.util.List;

public interface CacheStatisticsProvider {

    List<CacheStatistics> getStats();
}
