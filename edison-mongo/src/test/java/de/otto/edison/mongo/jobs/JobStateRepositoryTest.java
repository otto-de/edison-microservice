package de.otto.edison.mongo.jobs;

import com.github.fakemongo.Fongo;
import de.otto.edison.jobs.repository.JobStateRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobStateRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class JobStateRepositoryTest {

    @Parameters(name = "{0}")
    public static Collection<JobStateRepository> data() {
        return Arrays.asList(new MongoJobStateRepository(new Fongo("inMemoryDb").getDatabase("jobstate")),
                new InMemJobStateRepository());
    }

    private JobStateRepository testee;

    public JobStateRepositoryTest(JobStateRepository testee) {
        this.testee = testee;
    }

    @Test
    public void shouldStoreAndGetValue() throws Exception {
        testee.setValue("someJob", "someKey", "someValue");
        testee.setValue("someJob", "someOtherKey", "someDifferentValue");

        testee.setValue("someOtherJob", "someKey", "someOtherValue");

        assertThat(testee.getValue("someJob", "someKey"), is("someValue"));
        assertThat(testee.getValue("someJob", "someOtherKey"), is("someDifferentValue"));
        assertThat(testee.getValue("someOtherJob", "someKey"), is("someOtherValue"));
    }

    @Test
    public void shouldReturnNullForMissingKeys() throws Exception {
        testee.setValue("someJob", "someKey", "someValue");

        assertThat(testee.getValue("someJob", "someMissingKey"), is(nullValue()));
        assertThat(testee.getValue("someMissingJob", "someMissingKey"), is(nullValue()));
    }


}
