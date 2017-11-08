package de.otto.edison.logging.ui;

import de.otto.edison.navigation.NavBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.http.ActuatorMediaType;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint.LoggerLevels;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static de.otto.edison.navigation.NavBarItem.navBarItem;
import static de.otto.edison.util.UrlHelper.baseUriOf;
import static java.util.stream.Collectors.toList;
import static org.springframework.boot.logging.LogLevel.valueOf;
import static org.springframework.http.MediaType.*;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;


/**
 * Replacement for {@link org.springframework.boot.actuate.logging.LoggersEndpoint} with additional
 * HTML UI to configure the log levels of the microservice.
 *
 * @since 1.1.0
 */
@Controller
@ConditionalOnProperty(prefix = "edison.logging.ui", name = "enabled", matchIfMissing = true)
public class LoggersHtmlEndpoint {

    private final LoggersEndpoint loggersEndpoint;
    private final WebEndpointProperties webEndpointProperties;

    @Autowired
    public LoggersHtmlEndpoint(final LoggersEndpoint loggersEndpoint,
                               final NavBar rightNavBar,
                               final WebEndpointProperties  webEndpointProperties) {
        this.loggersEndpoint = loggersEndpoint;
        this.webEndpointProperties = webEndpointProperties;
        rightNavBar.register(navBarItem(1, "Loggers", String.format("%s/loggers", webEndpointProperties.getBasePath())));
    }

    @RequestMapping(
            value = "${management.endpoints.web.base-path}/loggers",
            produces = {
                    TEXT_HTML_VALUE,
                    ALL_VALUE},
            method = GET)
    public ModelAndView get(final HttpServletRequest request) {
        return new ModelAndView("loggers", new HashMap<String,Object>() {{
            put("loggers", getLoggers());
            put("baseUri", baseUriOf(request));
        }});
    }

    @RequestMapping(
            value = "${management.endpoints.web.base-path}/loggers",
            produces = {
                    ActuatorMediaType.V2_JSON,
                    APPLICATION_JSON_VALUE},
            method = GET)
    @ResponseBody
    public Object get() {
        final Map<String, Object> levels = loggersEndpoint.loggers();
        return (levels == null ? notFound().build() : levels);
    }

    @RequestMapping(
            value = "${management.endpoints.web.base-path}/loggers/{name:.*}",
            produces = {
                    ActuatorMediaType.V2_JSON,
                    APPLICATION_JSON_VALUE},
            method = GET)
    @ResponseBody
    public Object get(@PathVariable String name) {
        final LoggerLevels levels = loggersEndpoint.loggerLevels(name);
        return (levels == null ? notFound().build() : levels);
    }

    @RequestMapping(
            value = "${management.endpoints.web.base-path}/loggers",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = TEXT_HTML_VALUE,
            method = POST)
    public RedirectView post(@ModelAttribute("name") String name,
                             @ModelAttribute("level") String level,
                             HttpServletRequest httpServletRequest) {
        final LogLevel logLevel = level == null ? null : valueOf(level.toUpperCase());
        loggersEndpoint.configureLogLevel(name, logLevel);
        return new RedirectView(String.format("%s%s/loggers", baseUriOf(httpServletRequest), webEndpointProperties.getBasePath()));
    }

    @RequestMapping(
            value = "${management.endpoints.web.base-path}/loggers/{name:.*}",
            consumes = {
                    ActuatorMediaType.V2_JSON,
                    APPLICATION_JSON_VALUE},
            produces = {
                    ActuatorMediaType.V2_JSON,
                    APPLICATION_JSON_VALUE},
            method = POST)
    @ResponseBody
    public Object post(@PathVariable String name,
                       @RequestBody Map<String, String> configuration) {
        final String level = configuration.get("configuredLevel");
        final LogLevel logLevel = level == null ? null : LogLevel.valueOf(level.toUpperCase());
        loggersEndpoint.configureLogLevel(name, logLevel);
        return HttpEntity.EMPTY;
    }

    private List<Map<String,?>> getLoggers() {
        @SuppressWarnings({"unchecked", "raw"})
        final Map<String,?> loggers = (Map) loggersEndpoint.loggers().get("loggers");
        return loggers
                .keySet()
                .stream()
                .map(key -> key.contains("$") ? null : new HashMap<String,Object>() {{
                    final LoggerLevels logger = (LoggerLevels) loggers.get(key);
                    put("name", key);
                    put("displayName", displayNameOf(key));
                    put("configuredLevel", logger.getConfiguredLevel());
                    put("effectiveLevel", logger.getEffectiveLevel());
                }})
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private String displayNameOf(final String key) {

        if (key.contains(".")) {
            StringTokenizer tokenizer = new StringTokenizer(key, ".");
            StringJoiner joiner = new StringJoiner(".");
            int pos = 0;
            while (tokenizer.hasMoreTokens()) {
                final String word = tokenizer.nextToken();
                if (tokenizer.hasMoreTokens() && pos > 1) {
                    joiner.add(word.substring(0,1));
                } else {
                    joiner.add(word);
                }
                ++pos;
            }
            return joiner.toString();
        } else {
            return key;
        }
    }
}
