package de.otto.edison.status.configuration;

import de.otto.edison.status.domain.ApplicationStatus;
import de.otto.edison.status.indicator.ApplicationStatusAggregator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static de.otto.edison.status.domain.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {SystemInfoConfiguration.class, PropertyPlaceholderAutoConfiguration.class,
        ApplicationInfoConfiguration.class, VersionInfoConfiguration.class, TeamInfoConfiguration.class,
        ApplicationStatusAggregatorConfiguration.class})
public class ApplicationStatusAggregatorConfigurationTest {

    @Autowired
    private ApplicationStatusAggregator applicationStatusAggregator;

    private ApplicationStatus status;

    @Before
    public void setUp() {
        status = applicationStatusAggregator.aggregatedStatus();
    }

    @Test
    public void checkOverallStatus() {
        assertThat(status.status, is(OK));
        assertThat(status.application, is(notNullValue()));
        assertThat(status.system, is(notNullValue()));
        assertThat(status.vcs, is(notNullValue()));
        assertThat(status.team, is(notNullValue()));
        assertThat(status.statusDetails.isEmpty(), is(true));
        assertThat(status.serviceSpecs.isEmpty(), is(true));
    }

}
