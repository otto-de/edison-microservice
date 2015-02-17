package de.otto.µservice.status.controller;

import de.otto.µservice.testsupport.util.JsonMap;
import org.testng.annotations.Test;

import static de.otto.µservice.status.controller.ApplicationStatusRepresentation.statusRepresentationOf;
import static de.otto.µservice.status.domain.ApplicationStatus.detailedStatus;
import static de.otto.µservice.status.domain.Status.OK;
import static de.otto.µservice.status.domain.Status.WARNING;
import static de.otto.µservice.status.domain.StatusDetail.statusDetail;
import static de.otto.µservice.testsupport.util.JsonMap.jsonMapFrom;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ApplicationStatusRepresentationTest {

    @Test
    public void shouldCreateStatusRepresentationWithoutDetails() {
        // given
        final ApplicationStatusRepresentation representation = statusRepresentationOf(
                detailedStatus("app", emptyList())
        );
        // when
        final JsonMap jsonMap = jsonMapFrom(representation.getApplication());
        // then
        assertThat(jsonMap.getString("name"), is("app"));
        assertThat(jsonMap.getString("status"), is("OK"));
        assertThat(jsonMap.get("statusDetails").asMap().size(), is(0));
    }

    @Test
    public void shouldCreateStatusRepresentationWithSingleDetail() {
        // given
        final ApplicationStatusRepresentation representation = statusRepresentationOf(
                detailedStatus("app", asList(statusDetail("someDetail", WARNING, "detailed warning")))
        );
        // when
        final JsonMap jsonMap = jsonMapFrom(representation.getApplication());
        // then
        assertThat(jsonMap.getString("name"), is("app"));
        assertThat(jsonMap.getString("status"), is("WARNING"));
        assertThat(jsonMap.get("statusDetails").get("someDetail").getString("status"), is("WARNING"));
        assertThat(jsonMap.get("statusDetails").get("someDetail").getString("message"), is("detailed warning"));
    }

    @Test
    public void shouldCreateStatusRepresentationWithMultipleDetails() {
        // given
        final ApplicationStatusRepresentation representation = statusRepresentationOf(
                detailedStatus("app", asList(
                        statusDetail("someDetail", OK, "perfect"),
                        statusDetail("someOtherDetail", WARNING, "detailed warning"))
                )
        );
        // when
        final JsonMap jsonMap = jsonMapFrom(representation.getApplication());
        // then
        assertThat(jsonMap.getString("name"), is("app"));
        assertThat(jsonMap.getString("status"), is("WARNING"));
        assertThat(jsonMap.get("statusDetails").get("someDetail").getString("status"), is("OK"));
        assertThat(jsonMap.get("statusDetails").get("someOtherDetail").getString("status"), is("WARNING"));
    }
}
