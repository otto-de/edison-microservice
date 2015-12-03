package de.otto.edison.cachestatistics;

import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;

public class CacheStatisticsControllerTest {

    @Test
    public void shouldNotFailIfNotCacheStatsProviderCanBeFound() throws Exception {
        // Given
        CacheStatisticsController cacheStatisticsController = new CacheStatisticsController();

        // When
        cacheStatisticsController.getStats(mock(HttpServletResponse.class));
    }
}