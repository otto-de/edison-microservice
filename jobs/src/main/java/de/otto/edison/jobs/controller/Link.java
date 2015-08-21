package de.otto.edison.jobs.controller;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author Guido Steinacker
 * @since 21.08.15
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Link {

    public final String href;
    public final String rel;
    public final String title;

    private Link(final String href, final String rel, final String title) {
        this.href = href;
        this.rel = rel;
        this.title = title;
    }

    public static Link link(final String rel, final String href, final String title) {
        return new Link(href, rel, title);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (href != null ? !href.equals(link.href) : link.href != null) return false;
        if (rel != null ? !rel.equals(link.rel) : link.rel != null) return false;
        if (title != null ? !title.equals(link.title) : link.title != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = href != null ? href.hashCode() : 0;
        result = 31 * result + (rel != null ? rel.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Link{" +
                "href='" + href + '\'' +
                ", rel='" + rel + '\'' +
                ", title='" + title + '\'' +
                '}';
    }
}
