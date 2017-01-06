package de.otto.edison.status.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.otto.edison.status.domain.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusRepresentation {

    private static final Pattern STATUS_DETAIL_JSON_SEPARATOR_PATTERN = Pattern.compile("\\s(.)");

    public ApplicationRepresentation application;
    public SystemInfo system;
    public TeamInfo team;
    public List<ServiceSpec> serviceSpecs;

    private StatusRepresentation(final ApplicationStatus applicationStatus, final ClusterInfo clusterInfo) {
        this.application = new ApplicationRepresentation(applicationStatus, clusterInfo);
        this.system = applicationStatus.system;
        this.team = applicationStatus.team;
        this.serviceSpecs = applicationStatus.serviceSpecs;
    }

    public static StatusRepresentation statusRepresentationOf(final ApplicationStatus status, final ClusterInfo clusterInfo) {
        return new StatusRepresentation(status, clusterInfo);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ApplicationRepresentation {
        public String name;
        public String description;
        public String group;
        public String environment;
        public String version;
        public String commit;
        public String vcsUrl;
        public String staging;
        public String color;
        public Status status;
        public Map<String,?> statusDetails;

        public ApplicationRepresentation() {
        }

        private ApplicationRepresentation(final ApplicationStatus applicationStatus, final ClusterInfo clusterInfo) {
            this.name = applicationStatus.application.name;
            this.description = applicationStatus.application.description;
            this.group = applicationStatus.application.group;
            this.environment = applicationStatus.application.environment;
            this.version = applicationStatus.vcs.version;
            this.commit = applicationStatus.vcs.commit;
            this.vcsUrl = applicationStatus.vcs.url;
            this.staging = clusterInfo.staging;
            this.color = clusterInfo.color;
            this.status = applicationStatus.status;
            this.statusDetails = statusDetailsOf(applicationStatus.statusDetails);
        }
    }
}
