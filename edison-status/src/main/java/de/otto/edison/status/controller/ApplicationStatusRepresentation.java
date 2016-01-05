package de.otto.edison.status.controller;

import java.util.Date;
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

    private static final Date SYSTEM_START_TIME = new Date();
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
            put("hostname", applicationStatus.getSystemInfo().getHostName());
            put("port", applicationStatus.getSystemInfo().getPort());
            put("systemTime", new Date());
            put("systemStartTime", SYSTEM_START_TIME);
        }};

    }

    public Map<String, ?> getApplication() {
        return new LinkedHashMap<String, Object>() {{
            put("name", applicationStatus.getApplicationInfo().getName());
            put("description", applicationStatus.getApplicationInfo().getDescription());
            put("group", applicationStatus.getApplicationInfo().getGroup());
            put("environment", applicationStatus.getApplicationInfo().getEnvironment());
            put("version", applicationStatus.getVersionInfo().getVersion());
            put("commit", applicationStatus.getVersionInfo().getCommit());
            put("vcs-url", applicationStatus.getVersionInfo().getVcsUrl());
            put("status", applicationStatus.getStatus().name());
            put("statusDetails", statusDetailsOf(applicationStatus));
        }};
    }

    private Map<String, ?> statusDetailsOf(final ApplicationStatus applicationStatus) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (StatusDetail entry : applicationStatus.getStatusDetails()) {
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
