package de.otto.edison.dynamodb.jobs;

import de.otto.edison.jobs.domain.JobMeta;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ComponentScan(basePackages = {"de.otto.edison.dynamodb"})
@EnableAutoConfiguration
@ActiveProfiles("test")
public class DynamoJobMetaRepositoryIntegrationTest {

    @Autowired
    private DynamoJobMetaRepository jobMetaRepository;

    @Before
    public void setUp() {
        jobMetaRepository.createTable();
        jobMetaRepository.deleteAll();
    }

    @Test
    public void shouldStoreAndGetValue() throws Exception {
        jobMetaRepository.setValue("someJob", "someKey", "someValue");
        jobMetaRepository.setValue("someJob", "someOtherKey", "someDifferentValue");

        jobMetaRepository.setValue("someOtherJob", "someKey", "someOtherValue");

        assertThat(jobMetaRepository.getValue("someJob", "someKey"), is("someValue"));
        assertThat(jobMetaRepository.getValue("someJob", "someOtherKey"), is("someDifferentValue"));
        assertThat(jobMetaRepository.getValue("someOtherJob", "someKey"), is("someOtherValue"));
    }

    @Test
    public void shouldGetEmptyJobMeta() {
        final JobMeta jobMeta = jobMetaRepository.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @Test
    public void shouldGetJobMetaForRunningJob() {
        jobMetaRepository.setRunningJob("someJob", "someId");
        final JobMeta jobMeta = jobMetaRepository.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(true));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @Test
    public void shouldGetJobMetaForDisabledJob() {
        jobMetaRepository.disable("someJob", "some comment");
        final JobMeta jobMeta = jobMetaRepository.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("some comment"));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @Test
    public void shouldGetJobMetaForDisabledJobWithProperties() {
        jobMetaRepository.disable("someJob", "some comment");
        jobMetaRepository.setValue("someJob", "someKey", "some value");
        final JobMeta jobMeta = jobMetaRepository.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(singletonMap("someKey", "some value")));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("some comment"));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }

    @Test
    public void shouldEnableJob() {
        jobMetaRepository.setValue("someJob", "disabled", "foo");

        jobMetaRepository.enable("someJob");

        assertThat(jobMetaRepository.getValue("someJob", "disabled"), is(nullValue()));
    }

    @Test
    public void shouldDisableJob() {
        jobMetaRepository.disable("someJob", "some comment");

        assertThat(jobMetaRepository.getValue("someJob", "disabled"), is("some comment"));
    }

    @Test
    public void shouldSetRunningJob() {
        jobMetaRepository.setRunningJob("someJob", "someId");

        assertThat(jobMetaRepository.getRunningJob("someJob"), is("someId"));
        assertThat(jobMetaRepository.getValue("someJob", "running"), is("someId"));
    }

    public void shouldDeleteAll() {
        jobMetaRepository.enable("foo");
        jobMetaRepository.enable("bar");

        jobMetaRepository.deleteAll();

        assertThat(jobMetaRepository.findAllJobTypes(), is(empty()));
    }

    @Test
    public void shouldClearRunningJob() {
        jobMetaRepository.setValue("someJob", "running", "someId");

        jobMetaRepository.clearRunningJob("someJob");

        assertThat(jobMetaRepository.getRunningJob("someJob"), is(nullValue()));
        assertThat(jobMetaRepository.getValue("someJob", "runnin"), is(nullValue()));
    }

    @Test
    public void shouldReturnNullForMissingKeys() throws Exception {
        jobMetaRepository.setValue("someJob", "someKey", "someValue");

        assertThat(jobMetaRepository.getValue("someJob", "someMissingKey"), is(nullValue()));
        assertThat(jobMetaRepository.getValue("someMissingJob", "someMissingKey"), is(nullValue()));
    }

    @Test
    public void shouldFindJobTypes() {
        jobMetaRepository.setValue("someJob", "someKey", "someValue");
        jobMetaRepository.setValue("someOtherJob", "someKey", "someOtherValue");

        assertThat(jobMetaRepository.findAllJobTypes(), containsInAnyOrder("someJob", "someOtherJob"));
    }

    @Test
    public void shouldNotCreateIfExists() throws Exception {
        // given
        jobMetaRepository.setValue("someJob", "someKey", "initialValue");

        // when
        final boolean value = jobMetaRepository.createValue("someJob", "someKey", "newValue");

        //then
        assertThat(value, is(false));
        assertThat(jobMetaRepository.getValue("someJob", "someKey"), is("initialValue"));
    }

    @Test
    public void shouldCreateIfNotExists() throws Exception {
        // when
        final boolean value = jobMetaRepository.createValue("someJob", "someKey", "someValue");

        //then
        assertThat(value, is(true));
        assertThat(jobMetaRepository.getValue("someJob", "someKey"), is("someValue"));
    }

    @Test
    public void shouldCreateTwoValuesWithoutException() throws Exception {
        // given
        jobMetaRepository.createValue("someJob", "someKey", "someValue");

        // when
        final boolean value = jobMetaRepository.createValue("someJob", "someOtherKey", "someOtherValue");

        //then
        assertThat(value, is(true));
        assertThat(jobMetaRepository.getValue("someJob", "someKey"), is("someValue"));
        assertThat(jobMetaRepository.getValue("someJob", "someOtherKey"), is("someOtherValue"));
    }

    @Test
    public void shouldReturnFalseIfCreateWasCalledTwice() throws Exception {
        // given
        jobMetaRepository.createValue("someJob", "someKey", "someInitialValue");

        // when
        final boolean value = jobMetaRepository.createValue("someJob", "someKey", "someValue");

        //then
        assertThat(value, is(false));
        assertThat(jobMetaRepository.getValue("someJob", "someKey"), is("someInitialValue"));
    }

    @Test
    public void shouldNotKillOldFieldsOnCreate() throws Exception {
        // given
        jobMetaRepository.setValue("someJob", "someKey", "someInitialValue");

        // when
        final boolean value = jobMetaRepository.createValue("someJob", "someAtomicKey", "someValue");

        //then
        assertThat(value, is(true));
        assertThat(jobMetaRepository.getValue("someJob", "someKey"), is("someInitialValue"));
        assertThat(jobMetaRepository.getValue("someJob", "someAtomicKey"), is("someValue"));
    }

    @Test
    public void shouldUnsetKeyOnSetNullValue() {
        // given
        jobMetaRepository.setValue("someJob", "someKey", "someValue");

        // when
        jobMetaRepository.setValue("someJob", "someKey", null);

        // then
        assertThat(jobMetaRepository.findAllJobTypes(), contains("someJob"));
        assertThat(jobMetaRepository.getValue("someJob", "someKey"), is(nullValue()));
    }
}
