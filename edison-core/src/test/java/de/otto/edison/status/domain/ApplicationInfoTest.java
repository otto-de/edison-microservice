package de.otto.edison.status.domain;

import org.junit.Test;

import static de.otto.edison.status.configuration.ApplicationInfoProperties.applicationInfoProperties;

/**
 * Created by guido on 08.01.16.
 */
public class ApplicationInfoTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToConstructWithOutName() {
        // given
        ApplicationInfo applicationInfo = ApplicationInfo.applicationInfo("", applicationInfoProperties("", "", "", ""));
    }

}