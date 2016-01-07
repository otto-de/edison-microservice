package de.otto.edison.status.domain;

import org.testng.annotations.Test;

import static de.otto.edison.status.domain.VersionInfo.versionInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class VersionInfoTest {


    @Test
    public void shouldReplaceCommitHashInTemplate() {
        // given
        VersionInfo versionInfo = versionInfo("42.0.1-RELEASE", "ab0816", "http://example.org/test/{commit}");
        // then
        assertThat(versionInfo.url, is("http://example.org/test/ab0816"));
    }

    @Test
    public void shouldReplaceVersionInTemplate() {
        // given
        VersionInfo versionInfo = versionInfo("42.0.1-RELEASE", "ab0816", "http://example.org/test/{version}");
        // then
        assertThat(versionInfo.url, is("http://example.org/test/42.0.1-RELEASE"));
    }

}