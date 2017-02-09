package de.otto.edison.mongo.jobs;

import com.github.fakemongo.Fongo;
import de.otto.edison.jobs.repository.JobStateRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobStateRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class JobStateRepositoryTest {

    @Parameters(name = "{0}")
    public static Collection<JobStateRepository> data() {
        return asList(
                new MongoJobStateRepository(new Fongo("inMemoryDb").getDatabase("jobstate")),
                new InMemJobStateRepository());
    }

    @Before
    public void setUp() throws Exception {
        testee.deleteAll();

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

    @Test
    public void shouldFindJobTypes() {
        testee.setValue("someJob", "someKey", "someValue");
        testee.setValue("someOtherJob", "someKey", "someOtherValue");

        assertThat(testee.findAllJobTypes(), containsInAnyOrder("someJob", "someOtherJob"));
    }

    @Test
    public void shouldNotCreateIfExists() throws Exception {
        // given
        testee.setValue("someJob","someKey", "initialValue");

        // when
        boolean value = testee.createValue("someJob", "someKey", "newValue");

        //then
        assertThat(value, is(false));
        assertThat(testee.getValue("someJob", "someKey"), is("initialValue"));
    }

    @Test
    public void shouldCreateIfNotExists() throws Exception {
        // when
        boolean value = testee.createValue("someJob", "someKey", "someValue");

        //then
        assertThat(value, is(true));
        assertThat(testee.getValue("someJob", "someKey"), is("someValue"));
    }

    @Test
    public void shouldCreateTwoValuesWithoutException() throws Exception {
        // given
        testee.createValue("someJob", "someKey", "someValue");

        // when
        boolean value = testee.createValue("someJob", "someOtherKey", "someOtherValue");

        //then
        assertThat(value, is(true));
        assertThat(this.testee.getValue("someJob", "someKey"), is("someValue"));
        assertThat(this.testee.getValue("someJob", "someOtherKey"), is("someOtherValue"));
    }

    @Test
    public void shouldReturnFalseIfCreateWasCalledTwice() throws Exception {
        // given
        testee.createValue("someJob", "someKey", "someInitialValue");

        // when
        boolean value = testee.createValue("someJob", "someKey", "someValue");

        //then
        assertThat(value, is(false));
        assertThat(testee.getValue("someJob", "someKey"), is("someInitialValue"));
    }

    @Test
    public void shouldNotKillOldFieldsOnCreate() throws Exception {
        // given
        testee.setValue("someJob", "someKey", "someInitialValue");

        // when
        boolean value = testee.createValue("someJob", "someAtomicKey", "someValue");

        //then
        assertThat(value, is(true));
        assertThat(testee.getValue("someJob", "someKey"), is("someInitialValue"));
        assertThat(testee.getValue("someJob", "someAtomicKey"), is("someValue"));
    }

    @Test
    public void shouldUnsetKeyOnSetNullValue() {
        // given
        testee.setValue("someJob", "someKey", "someValue");

        // when
        testee.setValue("someJob", "someKey", null);

        // then
        assertThat(testee.findAllJobTypes(), contains("someJob"));
        assertThat(testee.getValue("someJob", "someKey"), is(nullValue()));
    }
}
