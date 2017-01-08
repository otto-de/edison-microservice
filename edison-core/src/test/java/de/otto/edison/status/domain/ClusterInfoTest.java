package de.otto.edison.status.domain;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ClusterInfoTest {

    @Test
    public void shouldGetClusterInfo() {
        final ClusterInfo clusterInfo = new ClusterInfo(
                () -> "Foo",
                () -> "Bar"
        );

        assertThat(clusterInfo.getColor(), is("Foo"));
        assertThat(clusterInfo.getColorState(), is("Bar"));
    }
}