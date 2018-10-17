package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static de.otto.edison.status.domain.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        SystemInfoConfiguration.class,
        ClusterInfoConfiguration.class,
        ApplicationInfoConfiguration.class,
        VersionInfoConfiguration.class,
        TeamInfoConfiguration.class,
        ApplicationStatusAggregatorConfiguration.class})
@ActiveProfiles("test")
public class ApplicationStatusAggregatorConfigurationTest {

    @Autowired
    private ApplicationStatusAggregator applicationStatusAggregator;

    private ApplicationStatus status;

    @BeforeEach
    public void setUp() {
        status = applicationStatusAggregator.aggregatedStatus();
    }

    @Test
    public void checkOverallStatus() {
        assertThat(status.status, is(OK));
        assertThat(status.application, is(notNullValue()));
        assertThat(status.cluster, is(notNullValue()));
        assertThat(status.system, is(notNullValue()));
        assertThat(status.vcs, is(notNullValue()));
        assertThat(status.team, is(notNullValue()));
        assertThat(status.statusDetails.isEmpty(), is(true));
    }

}
