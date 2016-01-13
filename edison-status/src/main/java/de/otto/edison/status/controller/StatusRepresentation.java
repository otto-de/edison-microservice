package de.otto.edison.status.controller;

import de.otto.edison.status.domain.*;
import net.jcip.annotations.Immutable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Immutable
final class StatusRepresentation {

    private static final Pattern STATUS_DETAIL_JSON_SEPARATOR_PATTERN = Pattern.compile("\\s(.)");

    class ApplicationRepresentation {
        public final String appId;
        public final String name;
        public final String description;
        public final String group;
        public final String environment;
        public final String version;
        public final String commit;
        public final String vcsUrl;
        public final Status status;
        public final Map<String,?> statusDetails;
        private ApplicationRepresentation(final ApplicationStatus applicationStatus) {
            this.appId = applicationStatus.application.appId;
            this.name = applicationStatus.application.name;
            this.description = applicationStatus.application.description;
            this.group = applicationStatus.application.group;
            this.environment = applicationStatus.application.environment;
            this.version = applicationStatus.vcs.version;
            this.commit = applicationStatus.vcs.commit;
            this.vcsUrl = applicationStatus.vcs.url;
            this.status = applicationStatus.status;
            this.statusDetails = statusDetailsOf(applicationStatus.statusDetails);
        }
    }
    public final ApplicationRepresentation application;
    public final SystemInfo system;
    public final TeamInfo team;
    public final List<ServiceSpec> serviceSpecs;

    private StatusRepresentation(final ApplicationStatus applicationStatus) {
        this.application = new ApplicationRepresentation(applicationStatus);
        this.system = applicationStatus.system;
        this.team = applicationStatus.team;
        this.serviceSpecs = applicationStatus.serviceSpecs;
    }

    public static StatusRepresentation statusRepresentationOf(final ApplicationStatus status) {
        return new StatusRepresentation(status);
    }

    private Map<String, ?> statusDetailsOf(final List<StatusDetail> statusDetails) {
        final Map<String, Object> map = new LinkedHashMap<>();
        for (StatusDetail entry : statusDetails) {
            map.put(toCamelCase(entry.getName()), new LinkedHashMap<String, String>() {{
                put("status", entry.getStatus().name());
                put("message", entry.getMessage());
                putAll(entry.getDetails().entrySet().stream().collect(Collectors.toMap(entry -> toCamelCase(entry.getKey()), Map.Entry::getValue)));
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
