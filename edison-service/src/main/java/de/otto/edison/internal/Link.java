package de.otto.edison.internal;

/**
 * A link to a page.
 *
 * @author Guido Steinacker
 * @since 03.12.15
 */
public class Link {
    public String href;
    public String title;

    public static Link link(final String href, final String title) {
        Link link = new Link();
        link.href = href;
        link.title = title;
        return link;
    }
}
