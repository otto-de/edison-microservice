package de.otto.edison.cachestatistics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping("/internal")
public class CacheStatisticsController {

    @Autowired(required = false)
    private List<CacheStatisticsProvider> cacheStatisticsProviders;

    @RequestMapping(value = "/cachestatistics", method = GET)
    public ModelAndView getStats(final HttpServletResponse response) {
        response.setHeader(CACHE_CONTROL, "no-store");

        final List<CacheStatistics> cacheValues = new ArrayList<>();
        if (cacheStatisticsProviders != null) {
            cacheStatisticsProviders.forEach(cachingStatsProvider -> cacheValues.addAll(cachingStatsProvider.getStats()));
        }

        return new ModelAndView("/internal/cachestatistics", new ModelMap("cacheInfos", cacheValues));
    }
}
