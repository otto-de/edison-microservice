package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import org.testng.annotations.Test;

import static de.otto.edison.jobs.domain.JobInfoBuilder.jobInfoBuilder;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isAbsent;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static java.net.URI.create;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;

public class InMemJobInfoRepositoryTest {

    @Test
    public void shouldFindStatusByUri() {
        // given
        final InMemJobRepository repository = new InMemJobRepository();
        // when
        final JobInfo job = jobInfoBuilder(()->"MYJOB", create("/jobs/" + randomUUID())).build();
        repository.createOrUpdate(job);
        // then
        assertThat(repository.findBy(job.getJobUri()), isPresent());
    }

    @Test
    public void shouldReturnAbsentStatus() {
        InMemJobRepository repository = new InMemJobRepository();
        assertThat(repository.findBy(create("/foo/bar")), isAbsent());
    }

}
