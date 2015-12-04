package de.otto.edison.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

import static de.otto.edison.internal.Link.link;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * Configuration of the /internal page, used to configure the sections and links contained on the page.
 *
 * @author Guido Steinacker
 * @since 1.0.0
 */
@Configuration
public class InternalPageConfiguration {

    public static final String ADMIN_SECTION = "Admin";

    @Bean
    @ConditionalOnMissingBean(name = "defaultAdminPageLinks")
    LinkGroup defaultAdminPageLinks() {
        return new LinkGroup() {
            @Override
            public int priority() {
                return 0;
            }

            @Override
            public String section() {
                return ADMIN_SECTION;
            }

            @Override
            public List<Link> links() {
                return asList(
                        link("/internal/status", "Status Page"),
                        link("/internal/jobs", "Jobs Overview"),
                        link("/internal/jobdefinitions", "Job Definitions"),
                        link("/internal/togglz", "Feature Toggles")
                );
            }
        };
    }

    @Bean
    public LinkGroup adminPageSection(final List<LinkGroup> allLinkGroups) {
        return new LinkGroup() {
            @Override
            public int priority() {
                return 0;
            }

            @Override
            public String section() {
                return ADMIN_SECTION;
            }

            @Override
            public List<Link> links() {
                return allLinkGroups
                        .stream()
                        .filter(group -> group.section().equals(ADMIN_SECTION))
                        .sorted(Comparator.comparing(LinkGroup::priority))
                        .flatMap(group->group.links().stream())
                        .collect(toList());
            }
        };
    }

}
