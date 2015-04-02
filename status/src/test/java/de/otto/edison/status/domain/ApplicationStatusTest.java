package de.otto.edison.status.domain;

import org.testng.annotations.Test;

import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static de.otto.edison.status.domain.Status.*;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static de.otto.edison.status.domain.VersionInfo.versionInfo;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ApplicationStatusTest {

    @Test
    public void shouldHaveStatusOkIfDetailsAreOk() {
        // given
        ApplicationStatus applicationStatus = applicationStatus("foo", "localhost", versionInfo("1", "none"), asList(
                        statusDetail("bar", OK, "a message"))
        );
        // then
        assertThat(applicationStatus.getName(), is("foo"));
        assertThat(applicationStatus.getStatus(), is(OK));
    }

    @Test
    public void shouldHaveStatusWarningIfDetailsContainWarnings() {
        // given
        ApplicationStatus applicationStatus = applicationStatus("foo", "localhost", versionInfo("1", "none"), asList(
                statusDetail("bar", OK, "a message"),
                statusDetail("foobar", WARNING, "another message")
        ));
        // then
        assertThat(applicationStatus.getStatus(), is(WARNING));
    }

    @Test
    public void shouldHaveStatusErrorIfDetailsContainWarnings() {
        // given
        ApplicationStatus applicationStatus = applicationStatus("foo", "localhost", versionInfo("1", "none"), asList(
                statusDetail("bar", OK, "a message"),
                statusDetail("foobar", ERROR, "another message"),
                statusDetail("foobar", WARNING, "yet another message")
        ));
        // then
        assertThat(applicationStatus.getStatus(), is(ERROR));
    }

}
