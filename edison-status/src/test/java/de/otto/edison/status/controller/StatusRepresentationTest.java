package de.otto.edison.status.controller;

import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.SystemInfo;
import de.otto.edison.status.domain.TeamInfo;
import de.otto.edison.status.domain.VersionInfo;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static de.otto.edison.status.controller.StatusRepresentation.statusRepresentationOf;
import static de.otto.edison.status.domain.ApplicationInfo.applicationInfo;
import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class StatusRepresentationTest {

    @Test
    public void shouldCreateStatusRepresentationWithoutDetails() {
        // given
        final StatusRepresentation json = statusRepresentationOf(
                applicationStatus(applicationInfo("app", "", "test", "local"), mock(SystemInfo.class), mock(VersionInfo.class), mock(TeamInfo.class), emptyList(), emptyList())
        );
        // then
        assertThat(json.application.name, is("app"));
        assertThat(json.application.status, is(OK));
        assertThat(json.application.statusDetails.size(), is(0));
    }

    @Test
    public void shouldCreateStatusRepresentationWithVersionInfo() {
        // given
        final StatusRepresentation json = statusRepresentationOf(
                applicationStatus(mock(ApplicationInfo.class), mock(SystemInfo.class), VersionInfo.versionInfo("1.0.0", "0815", "http://example.org/commits/{commit}"), mock(TeamInfo.class), emptyList(), emptyList())
        );
        // then
        assertThat(json.application.version, is("1.0.0"));
        assertThat(json.application.commit, is("0815"));
        assertThat(json.application.vcsUrl, is("http://example.org/commits/0815"));
    }

    @Test
    public void shouldCreateStatusRepresentationWithSingleDetail() {
        // given
        final StatusRepresentation json = statusRepresentationOf(
                applicationStatus(mock(ApplicationInfo.class), mock(SystemInfo.class), mock(VersionInfo.class), mock(TeamInfo.class), singletonList(
                        statusDetail("someDetail", WARNING, "detailed warning")), emptyList()
                )
        );
        // then
        assertThat(json.application.status, is(WARNING));
        @SuppressWarnings("unchecked")
        final Map<String, String> someDetail = (Map)json.application.statusDetails.get("someDetail");
        assertThat(someDetail.get("status"), is("WARNING"));
        assertThat(someDetail.get("message"), is("detailed warning"));
    }

    @Test
    public void shouldCreateStatusRepresentationWithMultipleDetails() {
        // given
    	final Map<String,String> detailMap = new HashMap<>();
    	detailMap.put("Count", "1000");
        final StatusRepresentation json = statusRepresentationOf(
                applicationStatus(mock(ApplicationInfo.class), mock(SystemInfo.class), mock(VersionInfo.class), mock(TeamInfo.class), asList(
                                statusDetail("Some Detail", OK, "perfect"),
                                statusDetail("Some Other Detail", WARNING, "detailed warning", detailMap)), emptyList()
                )
        );
        // then
        assertThat(json.application.status, is(WARNING));
        @SuppressWarnings("unchecked")
        final Map<String, String> someDetail = (Map)json.application.statusDetails.get("someDetail");
        assertThat(someDetail.get("status"), is("OK"));
        assertThat(someDetail.get("message"), is("perfect"));
        assertThat(someDetail.get("status"), is("OK"));
        @SuppressWarnings("unchecked")
        final Map<String, String> someOtherDetail = (Map)json.application.statusDetails.get("someOtherDetail");
        assertThat(someOtherDetail.get("status"), is("WARNING"));
        assertThat(someOtherDetail.get("message"), is("detailed warning"));
        assertThat(someOtherDetail.get("count"), is("1000"));
    }
}
