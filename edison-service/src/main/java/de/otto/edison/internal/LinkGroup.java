package de.otto.edison.internal;

import java.util.List;

/**
 * A group of links on a page.
 *
 * @author Guido Steinacker
 * @since 03.12.15
 */
public interface LinkGroup {

    /**
     * The priority is used to order multiple LinkGroups in a single section, lowest value first.
     *
     * @return 0..N
     */
    public int priority();

    /**
     * A section of links on a page. Example: 'Admin' and 'System' section on /internal/index.html.
     *
     * @return the name of a section on a page.
     */
    public String section();

    /**
     * The list of links added to a section.
     *
     * @return list of links
     */
    public List<Link> links();
}
