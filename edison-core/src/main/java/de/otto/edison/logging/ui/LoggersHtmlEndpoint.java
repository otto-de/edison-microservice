package de.otto.edison.logging.ui;

import de.otto.edison.configuration.EdisonApplicationProperties;
import de.otto.edison.navigation.NavBar;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
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
public class LoggersHtmlEndpoint {

    private final LoggersEndpoint loggersEndpoint;
    private final EdisonApplicationProperties applicationProperties;

    public LoggersHtmlEndpoint(final LoggersEndpoint loggersEndpoint,
                               final NavBar rightNavBar,
                               final EdisonApplicationProperties applicationProperties) {
        this.loggersEndpoint = loggersEndpoint;
        this.applicationProperties = applicationProperties;
        rightNavBar.register(navBarItem(1, "Loggers", String.format("%s/loggers", applicationProperties.getManagement().getBasePath())));
    }

    @RequestMapping(
            value = "${edison.application.management.base-path:/internal}/loggers",
            produces = {
                    TEXT_HTML_VALUE,
                    ALL_VALUE
            },
            method = GET)
    public ModelAndView get(final HttpServletRequest request) {
        return new ModelAndView("loggers", new HashMap<>() {{
            put("loggers", getLoggers());
            put("baseUri", baseUriOf(request));
        }});
    }

    @RequestMapping(
            value = "${edison.application.management.base-path:/internal}/loggers",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    @ResponseBody
    public Object get() {
        final LoggersEndpoint.LoggersDescriptor levels = loggersEndpoint.loggers();
        return (levels == null ? notFound().build() : levels);
    }

    @RequestMapping(
            value = "${edison.application.management.base-path:/internal}/loggers/{name:.*}",
            produces = APPLICATION_JSON_VALUE,
            method = GET)
    @ResponseBody
    public Object get(@PathVariable String name) {
        final LoggersEndpoint.LoggerLevelsDescriptor levels = loggersEndpoint.loggerLevels(name);
        return (levels == null ? notFound().build() : levels);
    }

    @RequestMapping(
            value = "${edison.application.management.base-path:/internal}/loggers",
            consumes = APPLICATION_FORM_URLENCODED_VALUE,
            produces = TEXT_HTML_VALUE,
            method = POST)
    public RedirectView post(@ModelAttribute("name") String name,
                             @ModelAttribute("level") String level,
                             HttpServletRequest httpServletRequest) {
        final LogLevel logLevel = level == null ? null : valueOf(level.toUpperCase());
        loggersEndpoint.configureLogLevel(name, logLevel);
        return new RedirectView(String.format("%s%s/loggers", baseUriOf(httpServletRequest), applicationProperties.getManagement().getBasePath()));
    }

    @RequestMapping(
            value = "${edison.application.management.base-path:/internal}/loggers/{name:.*}",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE,
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
        final Map<String,?> loggers = loggersEndpoint.loggers().getLoggers();
        return loggers
                .keySet()
                .stream()
                .map(key -> key.contains("$") ? null : new HashMap<String,Object>() {{
                    final LoggersEndpoint.SingleLoggerLevelsDescriptor logger = (LoggersEndpoint.SingleLoggerLevelsDescriptor) loggers.get(key);
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
