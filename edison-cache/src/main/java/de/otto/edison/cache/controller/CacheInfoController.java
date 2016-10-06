package de.otto.edison.cache.controller;

import de.otto.edison.annotations.Beta;
import de.otto.edison.cache.configuration.CaffeineCacheConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.CachePublicMetrics;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_UNDERSCORE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Controller that is responsible for serving cache metrics in JSON or HTML format.
 *
 * @since 0.76.0
 */
@Controller
@Beta
public class CacheInfoController {

    @Autowired
    CachePublicMetrics cacheMetrics;
    @Autowired(required = false)
    List<CaffeineCacheConfig> cacheConfigs;

    @RequestMapping(value = "/internal/cacheinfos", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String,CacheInfo> getCacheMetricsJson() {
        return enrichWithCacheSpecification(toCacheInfos(cacheMetrics));
    }

    @RequestMapping(value = "/internal/cacheinfos", method = GET, produces = MediaType.ALL_VALUE)
    @ResponseBody
    public ModelAndView getCacheMetricsHtml() {
        final Map<String, CacheInfo> metrics = getCacheMetricsJson();
        return new ModelAndView(
                "internal/cacheinfos", "cacheInfos",
                metrics.values()
                        .stream()
                        .sorted(comparing(CacheInfo::getName))
                        .collect(toList()));
    }

    private Map<String, CacheInfo> enrichWithCacheSpecification(final Map<String, CacheInfo> cacheInfos) {
        for (final String cacheName : cacheInfos.keySet()) {
            final Optional<CaffeineCacheConfig> cacheConfig = cacheConfigs
                    .stream()
                    .filter(c -> c.cacheName.equals(cacheName))
                    .findAny();
            if (cacheConfig.isPresent()) {
                cacheInfos.get(cacheName).setSpecification(cacheConfig.get().toMap());
            }
        }
        return cacheInfos;
    }

    private Map<String, CacheInfo> toCacheInfos(final CachePublicMetrics cacheMetrics) {
        final Map<String, CacheInfo> cacheInfos = new LinkedHashMap<>();
        cacheMetrics.metrics().forEach(m->{
            final String name = m.getName().substring("cache.".length());
            int pos = name.indexOf('.');
            final String cacheName = name.substring(0, pos);
            final String metricName = LOWER_UNDERSCORE.to(LOWER_CAMEL, name.substring(pos+1).replace(".", "_").replace("-", "_")) ;
            if (!cacheInfos.containsKey(cacheName)) {
                cacheInfos.put(cacheName, new CacheInfo(cacheName));
            }
            cacheInfos.get(cacheName).setMetric(metricName, m.getValue());
        });
        return cacheInfos;
    }
}
