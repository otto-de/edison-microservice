package de.otto.edison.status.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.domain.StatusDetail;
import net.jcip.annotations.Immutable;

@Immutable
final class ApplicationStatusRepresentation {

    private static final Pattern STATUS_DETAIL_JSON_SEPARATOR_PATTERN = Pattern.compile("\\s(.)");


    private final ApplicationStatus applicationStatus;

    private ApplicationStatusRepresentation(final ApplicationStatus applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public static ApplicationStatusRepresentation statusRepresentationOf(final ApplicationStatus status) {
        return new ApplicationStatusRepresentation(status);
    }

    public Map<String, ?> getSystem() {
        return new LinkedHashMap<String, Object>() {{
            put("hostname", applicationStatus.system.hostname);
            put("port", applicationStatus.system.port);
            put("systemTime", applicationStatus.system.getTime());
        }};

    }

    public Map<String, ?> getApplication() {
        return new LinkedHashMap<String, Object>() {{
            put("name", applicationStatus.application.name);
            put("version", applicationStatus.vcs.version);
            put("status", applicationStatus.status.name());
            put("statusDetails", statusDetailsOf(applicationStatus));
        }};
    }

    private Map<String, ?> statusDetailsOf(final ApplicationStatus applicationStatus) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (StatusDetail entry : applicationStatus.statusDetails) {
            map.put(toCamelCase(entry.getName()), new LinkedHashMap<String, String>() {{
                put("status", entry.getStatus().name());
                put("message", entry.getMessage());
                putAll(entry.getDetails().entrySet().stream().collect(Collectors.toMap(entry -> toCamelCase(entry.getKey()), entry -> entry.getValue())));
            }});
        }
        return map;

    }

	private static String toCamelCase(final String name) {
	    Matcher matcher = STATUS_DETAIL_JSON_SEPARATOR_PATTERN.matcher(name);
	    StringBuffer sb = new StringBuffer();
	    while (matcher.find()) {
	    	matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
	    }
	    matcher.appendTail(sb);
	    String s = sb.toString();
	    return s.substring(0,1).toLowerCase() + s.substring(1);
	}
}
