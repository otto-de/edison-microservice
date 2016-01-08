package de.otto.edison.status.domain;

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by guido on 08.01.16.
 */
public class ApplicationInfoTest {

    @Test
    public void shouldConstructAppId() {
        // given
        ApplicationInfo applicationInfo = ApplicationInfo.applicationInfo("name", "", "group", "env");
        // then
        assertThat(applicationInfo.appId, is("/env/group/name"));
    }

    @Test
    public void shouldConstructAppIdWithMissingGroup() {
        // given
        ApplicationInfo applicationInfo = ApplicationInfo.applicationInfo("name", "", "", "env");
        // then
        assertThat(applicationInfo.appId, is("/env/name"));
    }

    @Test
    public void shouldConstructAppIdWithOnlyNameSpecified() {
        // given
        ApplicationInfo applicationInfo = ApplicationInfo.applicationInfo("name", "", "", "");
        // then
        assertThat(applicationInfo.appId, is("/name"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void shouldFailToConstructWithOutName() {
        // given
        ApplicationInfo applicationInfo = ApplicationInfo.applicationInfo("", "", "", "");
    }

}