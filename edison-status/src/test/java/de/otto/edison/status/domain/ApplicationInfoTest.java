package de.otto.edison.status.domain;

import org.junit.Test;

/**
 * Created by guido on 08.01.16.
 */
public class ApplicationInfoTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailToConstructWithOutName() {
        // given
        ApplicationInfo applicationInfo = ApplicationInfo.applicationInfo("", "", "", "");
    }

}