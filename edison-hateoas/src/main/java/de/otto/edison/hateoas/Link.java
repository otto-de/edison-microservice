package de.otto.edison.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.annotations.Beta;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.lang.Boolean.TRUE;

/**
 * Created by guido on 05.07.16.
 */
@JsonInclude(NON_NULL)
@Beta
public class Link {

    public final String href;
    public final Boolean templated;
    public final String type;
    public final String hrefLang;
    public final String title;
    public final String name;
    public final String profile;
    public final Boolean deprecated;
    @JsonIgnore
    public final String rel;


    private Link(final String rel, final String href, final Boolean templated, final String type, final String hrefLang,
                 final String title, final String name, final String profile, final Boolean deprecated) {
        this.rel = rel;
        this.href = href;
        this.templated = templated;
        this.type = type;
        this.hrefLang = hrefLang;
        this.title = title;
        this.name = name;
        this.profile = profile;
        this.deprecated = deprecated;
    }

    public static Link selfLink(final String href) {
        return new Link("self", href, null, null, null, null, null, null, null);
    }

    public static Link link(final String rel, final String href) {
        return new Link(rel, href, null, null, null, null, null, null, null);
    }

    public static Link templatedLink(final String rel, final String uriTemplate) {
        return new Link(rel, uriTemplate, TRUE, null, null, null, null, null, null);
    }

    public static LinkBuilder templatedLinkBuilderFor(final String rel, final String uriTemplate) {
        return new LinkBuilder(rel, uriTemplate).beeingTemplated();
    }

    public static LinkBuilder linkBuilderFor(final String rel, final String href) {
        return new LinkBuilder(rel, href);
    }

    public static class LinkBuilder {
        private final String rel;
        private final String href;
        private String type;
        private String hrefLang;
        private String title;
        private String name;
        private String profile;
        private Boolean deprecated;
        private Boolean templated;

        public LinkBuilder(final String rel, final String href) {
            this.rel = rel;
            this.href = href;
        }

        public LinkBuilder withType(final String type) {
            this.type = type;
            return this;
        }

        public LinkBuilder withHrefLang(final String hrefLang) {
            this.hrefLang = hrefLang;
            return this;
        }

        public LinkBuilder withTitle(final String title) {
            this.title = title;
            return this;
        }

        public LinkBuilder withName(final String name) {
            this.name = name;
            return this;
        }

        public LinkBuilder withProfile(final String profile) {
            this.profile = profile;
            return this;
        }

        public LinkBuilder beeingDeprecated() {
            this.deprecated = TRUE;
            return this;
        }

        public LinkBuilder beeingTemplated() {
            this.templated = TRUE;
            return this;
        }

        public Link build() {
            return new Link(rel, href, templated, type, hrefLang, title, name, profile, deprecated);
        }
    }
}
