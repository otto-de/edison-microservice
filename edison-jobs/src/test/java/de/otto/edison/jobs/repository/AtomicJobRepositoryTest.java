package de.otto.edison.jobs.repository;

import de.otto.edison.jobs.service.JobRunLockProvider;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static de.otto.edison.jobs.repository.AtomicJobRepository.REPOSITORY_LOCK;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class AtomicJobRepositoryTest {

    private AtomicJobRepository subject;

    @Mock
    private JobRepository jobRepositoryMock;
    @Mock
    private JobRunLockProvider runlockProviderMock;
    @Mock
    private AtomicJobRepository.RepositoryRunnable<Void> runnableMock;

    @BeforeMethod
    public void setUp() throws Exception {
        initMocks(this);
        subject = new AtomicJobRepository(jobRepositoryMock, runlockProviderMock);
        when(runlockProviderMock.acquireRunLockForJobType(REPOSITORY_LOCK)).thenReturn(true);

    }

    @Test
    public void shouldLockRunAndRelease() {
        subject.atomicRepositoryOperation(runnableMock);

        InOrder inOrder = inOrder(jobRepositoryMock, runlockProviderMock, runnableMock);
        inOrder.verify(runlockProviderMock).acquireRunLockForJobType(REPOSITORY_LOCK);
        inOrder.verify(runnableMock).exec(jobRepositoryMock);
        inOrder.verify(runlockProviderMock).releaseRunLockForJobType(REPOSITORY_LOCK);
    }

    @Test
    public void shouldReleaseLockIfExceptionThrown() {
        doThrow(RuntimeException.class).when(runnableMock).exec(any());

        try {
            subject.atomicRepositoryOperation(runnableMock);
        } catch (RuntimeException ignored) {}

        verify(runlockProviderMock).releaseRunLockForJobType(REPOSITORY_LOCK);
    }

    @Test
    public void shouldRetryIfRepositoryIsBlocked() {


    }
}