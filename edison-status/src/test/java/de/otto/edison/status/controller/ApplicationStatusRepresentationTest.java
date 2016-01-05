package de.otto.edison.status.controller;

import static de.otto.edison.status.controller.ApplicationStatusRepresentation.statusRepresentationOf;
import static de.otto.edison.status.domain.ApplicationInfo.applicationInfo;
import static de.otto.edison.status.domain.ApplicationStatus.applicationStatus;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.Status.WARNING;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static de.otto.edison.status.domain.VersionInfo.versionInfo;
import static de.otto.edison.testsupport.util.JsonMap.jsonMapFrom;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import de.otto.edison.status.domain.ApplicationInfo;
import de.otto.edison.status.domain.SystemInfo;
import de.otto.edison.status.domain.VersionInfo;
import org.testng.annotations.Test;

import de.otto.edison.testsupport.util.JsonMap;

public class ApplicationStatusRepresentationTest {

    @Test
    public void shouldCreateStatusRepresentationWithoutDetails() {
        // given
        final ApplicationStatusRepresentation representation = statusRepresentationOf(
                applicationStatus(applicationInfo("app", "desc", "grp", "env"), mock(SystemInfo.class), mock(VersionInfo.class), emptyList())
        );
        // when
        final JsonMap jsonMap = jsonMapFrom(representation.getApplication());
        // then
        assertThat(jsonMap.getString("name"), is("app"));
        assertThat(jsonMap.getString("description"), is("desc"));
        assertThat(jsonMap.getString("group"), is("grp"));
        assertThat(jsonMap.getString("environment"), is("env"));
        assertThat(jsonMap.getString("status"), is("OK"));
        assertThat(jsonMap.get("statusDetails").asMap().size(), is(0));
    }

    @Test
    public void shouldCreateStatusRepresentationWithVersionInfo() {
        // given
        final ApplicationStatusRepresentation representation = statusRepresentationOf(
                applicationStatus(mock(ApplicationInfo.class), mock(SystemInfo.class), versionInfo("1.0.0", "0815", "http://example.org/commits/{commit}"), emptyList())
        );
        // when
        final JsonMap jsonMap = jsonMapFrom(representation.getApplication());
        // then
        assertThat(jsonMap.getString("version"), is("1.0.0"));
        assertThat(jsonMap.getString("commit"), is("0815"));
        assertThat(jsonMap.getString("vcs-url"), is("http://example.org/commits/0815"));
    }

    @Test
    public void shouldCreateStatusRepresentationWithSingleDetail() {
        // given
        final ApplicationStatusRepresentation representation = statusRepresentationOf(
                applicationStatus(mock(ApplicationInfo.class), mock(SystemInfo.class), mock(VersionInfo.class), asList(
                        statusDetail("someDetail", WARNING, "detailed warning"))
                )
        );
        // when
        final JsonMap jsonMap = jsonMapFrom(representation.getApplication());
        // then
        assertThat(jsonMap.getString("status"), is("WARNING"));
        assertThat(jsonMap.get("statusDetails").get("someDetail").getString("status"), is("WARNING"));
        assertThat(jsonMap.get("statusDetails").get("someDetail").getString("message"), is("detailed warning"));
    }

    @Test
    public void shouldCreateStatusRepresentationWithMultipleDetails() {
        // given
    	final Map<String,String> detailMap = new HashMap<String,String>();
    	detailMap.put("Count", "1000");
        final ApplicationStatusRepresentation representation = statusRepresentationOf(
                applicationStatus(mock(ApplicationInfo.class), mock(SystemInfo.class), mock(VersionInfo.class), asList(
                                statusDetail("Some Detail", OK, "perfect"),
                                statusDetail("Some Other Detail", WARNING, "detailed warning", detailMap))
                )
        );
        // when
        final JsonMap jsonMap = jsonMapFrom(representation.getApplication());
        // then
        assertThat(jsonMap.getString("status"), is("WARNING"));
        assertThat(jsonMap.get("statusDetails").get("someDetail").getString("status"), is("OK"));
        assertThat(jsonMap.get("statusDetails").get("someOtherDetail").getString("status"), is("WARNING"));
        assertThat(jsonMap.get("statusDetails").get("someOtherDetail").getString("count"), is("1000"));
    }
}
