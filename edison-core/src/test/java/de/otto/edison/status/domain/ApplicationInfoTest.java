package de.otto.edison.status.domain;

import org.junit.jupiter.api.Test;

import static de.otto.edison.configuration.EdisonApplicationProperties.edisonApplicationProperties;
import static de.otto.edison.status.domain.ApplicationInfo.applicationInfo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Created by guido on 08.01.16.
 */
public class ApplicationInfoTest {

    @Test
    public void shouldFailToConstructWithOutName() {
        // given / when / then
        assertThrows(IllegalArgumentException.class, () -> {
            applicationInfo("", edisonApplicationProperties("", "", "", ""));
        });
    }
}