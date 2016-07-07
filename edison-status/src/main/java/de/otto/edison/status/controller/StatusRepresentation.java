package de.otto.edison.status.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.otto.edison.hateoas.HalRepresentation;
import de.otto.edison.status.domain.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.otto.edison.hateoas.Link.linkBuilder;
import static de.otto.edison.hateoas.Link.profile;
import static de.otto.edison.hateoas.Link.self;
import static de.otto.edison.hateoas.Links.linkingTo;
import static de.otto.edison.status.controller.UrlHelper.absoluteHrefOf;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusRepresentation extends HalRepresentation {

    private static final Pattern STATUS_DETAIL_JSON_SEPARATOR_PATTERN = Pattern.compile("\\s(.)");

    public ApplicationRepresentation application;
    public SystemInfo system;
    public TeamInfo team;
    public List<ServiceSpec> serviceSpecs;

    private StatusRepresentation(final ApplicationStatus applicationStatus) {
        super(
                linkingTo(
                        self(absoluteHrefOf("/internal/status.json")),
                        profile("http://otto-de.github.io/profiles/monitoring/status"),
                        linkBuilder("http://otto-de.github.io/linkrelations/features", absoluteHrefOf("/internal/toggles.json"))
                                .withTitle("Feature Toggles")
                                .withType("application/hal+json")
                                .withProfile("http://otto-de.github.io/profiles/monitoring/features")
                                .build()
                )
        );
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    class ApplicationRepresentation {
        public String name;
        public String description;
        public String group;
        public String environment;
        public String version;
        public String commit;
        public String vcsUrl;
        public Status status;
        public Map<String,?> statusDetails;

        public ApplicationRepresentation() {
        }

        private ApplicationRepresentation(final ApplicationStatus applicationStatus) {
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
}
