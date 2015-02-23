package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.domain.JobInfo;
import de.otto.edison.jobs.domain.JobType;
import de.otto.edison.jobs.service.JobFactory;
import org.testng.annotations.Test;

import java.net.URI;

import static de.otto.edison.testsupport.matcher.OptionalMatchers.isAbsent;
import static de.otto.edison.testsupport.matcher.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;

public class InMemJobInfoRepositoryTest {

    enum MockJobType implements JobType { FOO }

    @Test
    public void shouldFindStatusByUri() {
        // given
        final InMemJobRepository repository = new InMemJobRepository();
        final JobFactory jobFactory = new JobFactory("/test");
        // when
        final JobInfo job = jobFactory.createJobInfo(MockJobType.FOO);
        repository.createOrUpdate(job);
        // then
        assertThat(repository.findBy(job.getJobUri()), isPresent());
    }

    @Test
    public void shouldReturnAbsentStatus() {
        InMemJobRepository repository = new InMemJobRepository();
        assertThat(repository.findBy(URI.create("/foo/bar")), isAbsent());
    }

}
