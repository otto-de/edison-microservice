package de.otto.µservice.jobs.repository;

import de.otto.µservice.jobs.domain.JobInfo;
import de.otto.µservice.jobs.domain.JobType;
import de.otto.µservice.jobs.service.JobFactory;
import org.testng.annotations.Test;

import java.net.URI;

import static de.otto.µservice.testsupport.matcher.OptionalMatchers.isAbsent;
import static de.otto.µservice.testsupport.matcher.OptionalMatchers.isPresent;
import static org.hamcrest.MatcherAssert.assertThat;

public class InMemJobInfoRepositoryTest {

    enum MockJobType implements JobType { FOO }

    @Test
    public void shouldFindStatusByUri() {
        // given
        final InMemJobRepository repository = new InMemJobRepository();
        final JobFactory jobFactory = new JobFactory("/test");
        // when
        final JobInfo job = jobFactory.createJob(MockJobType.FOO);
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
