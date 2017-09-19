package de.otto.edison.mongo.jobs;

import com.github.fakemongo.Fongo;
import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobMetaRepository;
import de.otto.edison.mongo.configuration.MongoProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class JobMetaRepositoryTest {

    @Parameters(name = "{0}")
    public static Collection<JobMetaRepository> data() {
        return asList(
                new MongoJobMetaRepository(new Fongo("inMemoryDb").getDatabase("jobmeta"),
                        "jobmeta",
                        new MongoProperties()),
                new InMemJobMetaRepository());
    }

    @Before
    public void setUp() throws Exception {
        testee.deleteAll();
    }

    private JobMetaRepository testee;

    public JobMetaRepositoryTest(JobMetaRepository testee) {
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
    public void shouldGetEmptyJobMeta() {
        JobMeta jobMeta = testee.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @Test
    public void shouldGetJobMetaForRunningJob() {
        testee.setRunningJob("someJob", "someId");
        JobMeta jobMeta = testee.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(true));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @Test
    public void shouldGetJobMetaForDisabledJob() {
        testee.disable("someJob", "some comment");
        JobMeta jobMeta = testee.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("some comment"));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @Test
    public void shouldGetJobMetaForDisabledJobWithProperties() {
        testee.disable("someJob", "some comment");
        testee.setValue("someJob", "someKey", "some value");
        JobMeta jobMeta = testee.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(singletonMap("someKey", "some value")));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("some comment"));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @Test
    public void shouldEnableJob() {
        testee.setValue("someJob", "_e_disabled", "foo");

        testee.enable("someJob");

        assertThat(testee.getValue("someJob", "_e_disabled"), is(nullValue()));
    }

    @Test
    public void shouldDisableJob() {
        testee.disable("someJob", "some comment");

        assertThat(testee.getValue("someJob", "_e_disabled"), is("some comment"));
    }

    @Test
    public void shouldSetRunningJob() {
        testee.setRunningJob("someJob", "someId");

        assertThat(testee.getRunningJob("someJob"), is("someId"));
        assertThat(testee.getValue("someJob", "_e_running"), is("someId"));
    }

    public void shouldDeleteAll() {
        testee.enable("foo");
        testee.enable("bar");

        testee.deleteAll();

        assertThat(testee.findAllJobTypes(), is(empty()));
    }

    @Test
    public void shouldClearRunningJob() {
        testee.setValue("someJob", "_e_running", "someId");

        testee.clearRunningJob("someJob");

        assertThat(testee.getRunningJob("someJob"), is(nullValue()));
        assertThat(testee.getValue("someJob", "_e_runnin"), is(nullValue()));
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
