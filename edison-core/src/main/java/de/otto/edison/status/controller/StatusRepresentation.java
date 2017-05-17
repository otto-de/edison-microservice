package de.otto.edison.status.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.status.domain.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_EMPTY)
public class StatusRepresentation {

    private static final Pattern STATUS_DETAIL_JSON_SEPARATOR_PATTERN = Pattern.compile("\\s(.)");

    public ApplicationRepresentation application;
    public ClusterInfo cluster;
    public SystemInfo system;
    public TeamInfo team;
    public Criticality criticality;
    public List<DependencyRepresentation> dependencies;

    private StatusRepresentation(final ApplicationStatus applicationStatus,
                                 final Criticality criticality,
                                 final List<ExternalDependency> dependencies) {
        this.application = new ApplicationRepresentation(applicationStatus);
        this.system = applicationStatus.system;
        this.team = applicationStatus.team;
        this.cluster = applicationStatus.cluster.isEnabled() ? applicationStatus.cluster : null;
        this.criticality = criticality;
        this.dependencies = dependencies != null
                ? dependencies
                        .stream()
                        .sorted(comparing(ExternalDependency::getName))
                        .map(DependencyRepresentation::new)
                        .collect(toList())
                : emptyList();
    }

    public static StatusRepresentation statusRepresentationOf(final ApplicationStatus status) {
        return new StatusRepresentation(status, null, null);
    }

    public static StatusRepresentation statusRepresentationOf(final ApplicationStatus status,
                                                              final Criticality criticality,
                                                              final List<ExternalDependency> dependencies) {
        return new StatusRepresentation(status, criticality, dependencies);
    }

	@JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(NON_EMPTY)
    static class DependencyRepresentation {
        public Expectations expectations;
        public Criticality criticality;
        public List<String> datasources = emptyList();
        public String url = "";
        public String methods = "";
        public List<String> mediatypes = emptyList();
        public String authentication = "";
        public String name;
        public String description;
        public String type;

        public DependencyRepresentation(final ExternalDependency dependency) {
            name = dependency.getName();
            description = dependency.getDescription();
            type = dependency.getType() + "/" + dependency.getSubtype();
            criticality = dependency.getCriticality();
            expectations = dependency.getExpectations();
            if (dependency instanceof ServiceDependency) {
                final ServiceDependency serviceDependency = (ServiceDependency) dependency;
                url = serviceDependency.getUrl();
                methods = valueOf(serviceDependency.getMethods());
                mediatypes = serviceDependency.getMediaTypes();
                authentication = serviceDependency.getAuthentication();
            } else {
                @SuppressWarnings("unchecked")
                final List<Datasource> datasources = ((DatasourceDependency) dependency).getDatasources();
                this.datasources = datasources.stream().map(Datasource::toString).collect(toList());
            }
        }

        <T> String valueOf(List<T> value) {
            if (value != null) {
                return value.stream().map(Object::toString).collect(joining(", "));
            }
            return "";
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ApplicationRepresentation {
        public String name;
        public String title;
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
            this.title = applicationStatus.application.title;
            this.description = applicationStatus.application.description;
            this.group = applicationStatus.application.group;
            this.environment = applicationStatus.application.environment;
            this.version = applicationStatus.vcs.version;
            this.commit = applicationStatus.vcs.commit;
            this.vcsUrl = applicationStatus.vcs.url;
            this.status = applicationStatus.status;
            this.statusDetails = statusDetailsOf(applicationStatus.statusDetails);
        }

        private Map<String, ?> statusDetailsOf(final List<StatusDetail> statusDetails) {
            final Map<String, Object> map = new LinkedHashMap<>();
            for (StatusDetail entry : statusDetails) {
                final List<Map<String, String>> links = toLinks(entry.getLinks());
                map.put(toCamelCase(entry.getName()), new LinkedHashMap<String, Object>() {{
                    put("status", entry.getStatus().name());
                    put("message", entry.getMessage());
                    put("links", links);
                    putAll(entry.getDetails().entrySet().stream().collect(Collectors.toMap(entry -> toCamelCase(entry.getKey()), Map.Entry::getValue)));
                }});
            }
            return map;

        }

        private List<Map<String, String>> toLinks(final List<Link> links) {
            final List<Map<String,String>> result = new ArrayList<>();
            links.forEach(link -> result.add(new LinkedHashMap<String,String>() {{
                put("rel", link.rel);
                put("href", link.href.startsWith("http")
                        ? link.href
                        : fromCurrentContextPath().path(link.href).build().toString());
                put("title", link.title);
            }}));
            return result;
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
}
