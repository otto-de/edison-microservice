package de.otto.edison.status.domain;

import org.junit.jupiter.api.Test;

import static de.otto.edison.status.domain.ClusterInfo.clusterInfo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClusterInfoTest {

    @Test
    public void shouldGetClusterInfo() {
        final ClusterInfo clusterInfo = clusterInfo(
                () -> "Foo",
                () -> "Bar"
        );

        assertThat(clusterInfo.getColor(), is("Foo"));
        assertThat(clusterInfo.getColorState(), is("Bar"));
    }
}