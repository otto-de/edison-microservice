package de.otto.edison.navigation;


import org.junit.Test;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class NavBarTest {

    @Test
    public void shouldSortNavBarItemsByIndex() {
        final NavBar navBar = NavBar.navBar(asList(
                NavBarItem.navBarItem(1, "Bar", "/"),
                NavBarItem.navBarItem(0, "Foo", "/")
        ));
        assertThat(navBar.getItems().get(0).getTitle(), is("Foo"));
        assertThat(navBar.getItems().get(1).getTitle(), is("Bar"));
    }

    @Test
    public void shouldSortNavBarItemsByIndexAndTitle() {
        final NavBar navBar = NavBar.navBar(asList(
                NavBarItem.navBarItem(0, "Foo", "/"),
                NavBarItem.navBarItem(0, "Bar", "/")
        ));
        assertThat(navBar.getItems().get(0).getTitle(), is("Bar"));
        assertThat(navBar.getItems().get(1).getTitle(), is("Foo"));
    }

    @Test
    public void shouldSortNavBarItemsAfterRegistration() {
        final NavBar navBar = NavBar.navBar(asList(
                NavBarItem.navBarItem(0, "Foo", "/"),
                NavBarItem.navBarItem(1, "Bar", "/")
        ));
        navBar.register(NavBarItem.navBarItem(1, "AAARGH", "/"));
        assertThat(navBar.getItems().get(0).getTitle(), is("Foo"));
        assertThat(navBar.getItems().get(1).getTitle(), is("AAARGH"));
        assertThat(navBar.getItems().get(2).getTitle(), is("Bar"));
    }
}