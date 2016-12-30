package de.otto.edison.navigation;

import static java.lang.Integer.MAX_VALUE;

/**
 * A single item in the navigation structure of Edison Microservices.
 * <p>
 *     The navigation structure is primarily used on /internal pages and consists of a <em>main</em> navigation (see
 *     templates/fragments/navbar/main.html) and a <em>right</em> dropdown navigation
 *     (see templates/fragments/navbar/right.html)
 * </p>
 * @since 1.0.0
 */
public class NavBarItem {

    private final int position;

    private final String title;
    private final String link;

    /**
     * Topmost position of an item inside of a NavBar.
     *
     * @return 0
     */
    public static int top() {
        return 0;
    }

    /**
     * Last position of an item inside of a NavBar.
     *
     * @return Integer.MAX_VALUE
     */
    public static int bottom() {
        return MAX_VALUE;
    }

    /**
     * Creates a new NavBarItem.
     *
     * @param position The position of the item inside the NavBar. Multiple items having the same position will be ordered by title.
     * @param title The human-readable title of the navigation item.
     * @param link The link of the navigation item.
     */
    private NavBarItem(final int position,
                       final String title,
                       final String link) {
        this.position = position;
        this.title = title;
        this.link = link;
    }

    public static NavBarItem navBarItem(final int position,
                                        final String title,
                                        final String link) {
        return new NavBarItem(position, title, link);
    }

    /**
     *
     * @return The position of the item inside the NavBar. Multiple items having the same position will be ordered by title.
     */
    public int getPosition() {
        return position;
    }

    /**
     *
     * @return The human-readable title of the navigation item.
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @return The link of the navigation item.
     */
    public String getLink() {
        return link;
    }
}
