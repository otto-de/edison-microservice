package de.otto.edison.navigation;

import net.jcip.annotations.ThreadSafe;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;

/**
 * The navigation of Edison-Microservices on /internal pages.
 * <p>
 *     The NavBar can be extended by more items. In order to do this, just {@link #register}
 * </p>
 * @since 1.0.0
 */
@ThreadSafe
public class NavBar {

    private volatile List<NavBarItem> items;

    private NavBar(final List<NavBarItem> items) {
        updateAndSortItems(new ArrayList<>(items));
    }

    public static NavBar emptyNavBar() {
        return new NavBar(emptyList());
    }

    public static NavBar navBar(final List<NavBarItem> items) {
        return new NavBar(items);
    }

    /**
     * Registers another item in this NavBar.
     * <p>
     *     The item is positioned in the navigation by {@link NavBarItem#getPosition()},
     *     then by {@link NavBarItem#getTitle()}.
     * </p>
     * @param item New NavBarItem.
     */
    public void register(final NavBarItem item) {
        updateAndSortItems(new ArrayList<NavBarItem>(items) {{
            add(item);
        }});
    }

    /**
     * Returns the list of all {@link NavBarItem items}.
     * <p>
     *     The list is sorted by {@link NavBarItem#getPosition()}, then by {@link NavBarItem#getTitle()}.
     * </p>
     * @return Unmodifiable list of navbar items.
     */
    public List<NavBarItem> getItems() {
        return unmodifiableList(items);
    }

    private void updateAndSortItems(final List<NavBarItem> items) {
        items.sort(
                comparing(NavBarItem::getPosition)
                        .thenComparing(NavBarItem::getTitle));
        this.items = unmodifiableList(items);
    }
}
