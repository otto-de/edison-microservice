package de.otto.edison.jobs.repository;

import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AggregateCleanupStrategyTest {

    @Mock
    JobRepository jobRepository;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldCallCleanupOnAllStrategies() {
        JobCleanupStrategy someCleanupStrategy = mock(JobCleanupStrategy.class);
        JobCleanupStrategy otherCleanupStrategy = mock(JobCleanupStrategy.class);

        new AggregateCleanupStrategy(someCleanupStrategy, otherCleanupStrategy).doCleanUp(jobRepository);

        verify(someCleanupStrategy).doCleanUp(jobRepository);
        verify(otherCleanupStrategy).doCleanUp(jobRepository);
    }
}