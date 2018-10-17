package de.otto.edison.mongo.jobs;

import de.otto.edison.jobs.domain.JobMeta;
import de.otto.edison.jobs.repository.JobMetaRepository;
import de.otto.edison.jobs.repository.inmem.InMemJobMetaRepository;
import de.otto.edison.mongo.configuration.MongoProperties;
import de.otto.edison.mongo.testsupport.EmbeddedMongoHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class JobMetaRepositoryTest {

    @AfterAll
    public static void teardownMongo() {
        EmbeddedMongoHelper.stopMongoDB();
    }

    @BeforeAll
    public static void startMongo() throws IOException {
        EmbeddedMongoHelper.startMongoDB();
    }

    public static Collection<JobMetaRepository> data() {
        return asList(
                new MongoJobMetaRepository(EmbeddedMongoHelper.getMongoClient().getDatabase("jobmeta-" + UUID.randomUUID()),
                        "jobmeta",
                        new MongoProperties()),
                new InMemJobMetaRepository()
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldStoreAndGetValue(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.setValue("someJob", "someKey", "someValue");
        testee.setValue("someJob", "someOtherKey", "someDifferentValue");

        testee.setValue("someOtherJob", "someKey", "someOtherValue");

        assertThat(testee.getValue("someJob", "someKey"), is("someValue"));
        assertThat(testee.getValue("someJob", "someOtherKey"), is("someDifferentValue"));
        assertThat(testee.getValue("someOtherJob", "someKey"), is("someOtherValue"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldGetEmptyJobMeta(final JobMetaRepository testee) {
        testee.deleteAll();
        final JobMeta jobMeta = testee.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldGetJobMetaForRunningJob(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.setRunningJob("someJob", "someId");
        final JobMeta jobMeta = testee.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(false));
        assertThat(jobMeta.getDisabledComment(), is(""));
        assertThat(jobMeta.isRunning(), is(true));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldGetJobMetaForDisabledJob(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.disable("someJob", "some comment");
        final JobMeta jobMeta = testee.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(emptyMap()));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("some comment"));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldGetJobMetaForDisabledJobWithProperties(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.disable("someJob", "some comment");
        testee.setValue("someJob", "someKey", "some value");
        final JobMeta jobMeta = testee.getJobMeta("someJob");

        assertThat(jobMeta.getAll(), is(singletonMap("someKey", "some value")));
        assertThat(jobMeta.isDisabled(), is(true));
        assertThat(jobMeta.getDisabledComment(), is("some comment"));
        assertThat(jobMeta.isRunning(), is(false));
        assertThat(jobMeta.getJobType(), is("someJob"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldEnableJob(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.setValue("someJob", "_e_disabled", "foo");

        testee.enable("someJob");

        assertThat(testee.getValue("someJob", "_e_disabled"), is(nullValue()));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldDisableJob(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.disable("someJob", "some comment");

        assertThat(testee.getValue("someJob", "_e_disabled"), is("some comment"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldSetRunningJob(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.setRunningJob("someJob", "someId");

        assertThat(testee.getRunningJob("someJob"), is("someId"));
        assertThat(testee.getValue("someJob", "_e_running"), is("someId"));
    }

    @ParameterizedTest
    @MethodSource("data")
    public void shouldDeleteAll(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.enable("foo");
        testee.enable("bar");

        testee.deleteAll();

        assertThat(testee.findAllJobTypes(), is(empty()));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldClearRunningJob(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.setValue("someJob", "_e_running", "someId");

        testee.clearRunningJob("someJob");

        assertThat(testee.getRunningJob("someJob"), is(nullValue()));
        assertThat(testee.getValue("someJob", "_e_runnin"), is(nullValue()));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldReturnNullForMissingKeys(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.setValue("someJob", "someKey", "someValue");

        assertThat(testee.getValue("someJob", "someMissingKey"), is(nullValue()));
        assertThat(testee.getValue("someMissingJob", "someMissingKey"), is(nullValue()));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldFindJobTypes(final JobMetaRepository testee) {
        testee.deleteAll();
        testee.setValue("someJob", "someKey", "someValue");
        testee.setValue("someOtherJob", "someKey", "someOtherValue");

        assertThat(testee.findAllJobTypes(), containsInAnyOrder("someJob", "someOtherJob"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldNotCreateIfExists(final JobMetaRepository testee) {
        testee.deleteAll();
        // given
        testee.setValue("someJob", "someKey", "initialValue");

        // when
        final boolean value = testee.createValue("someJob", "someKey", "newValue");

        //then
        assertThat(value, is(false));
        assertThat(testee.getValue("someJob", "someKey"), is("initialValue"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldCreateIfNotExists(final JobMetaRepository testee) {
        testee.deleteAll();
        // when
        final boolean value = testee.createValue("someJob", "someKey", "someValue");

        //then
        assertThat(value, is(true));
        assertThat(testee.getValue("someJob", "someKey"), is("someValue"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldCreateTwoValuesWithoutException(final JobMetaRepository testee) {
        testee.deleteAll();
        // given
        testee.createValue("someJob", "someKey", "someValue");

        // when
        final boolean value = testee.createValue("someJob", "someOtherKey", "someOtherValue");

        //then
        assertThat(value, is(true));
        assertThat(testee.getValue("someJob", "someKey"), is("someValue"));
        assertThat(testee.getValue("someJob", "someOtherKey"), is("someOtherValue"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldReturnFalseIfCreateWasCalledTwice(final JobMetaRepository testee) {
        testee.deleteAll();
        // given
        testee.createValue("someJob", "someKey", "someInitialValue");

        // when
        final boolean value = testee.createValue("someJob", "someKey", "someValue");

        //then
        assertThat(value, is(false));
        assertThat(testee.getValue("someJob", "someKey"), is("someInitialValue"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldNotKillOldFieldsOnCreate(final JobMetaRepository testee) {
        testee.deleteAll();
        // given
        testee.setValue("someJob", "someKey", "someInitialValue");

        // when
        final boolean value = testee.createValue("someJob", "someAtomicKey", "someValue");

        //then
        assertThat(value, is(true));
        assertThat(testee.getValue("someJob", "someKey"), is("someInitialValue"));
        assertThat(testee.getValue("someJob", "someAtomicKey"), is("someValue"));
    }


    @ParameterizedTest
    @MethodSource("data")
    public void shouldUnsetKeyOnSetNullValue(final JobMetaRepository testee) {
        testee.deleteAll();
        // given
        testee.setValue("someJob", "someKey", "someValue");

        // when
        testee.setValue("someJob", "someKey", null);

        // then
        assertThat(testee.findAllJobTypes(), contains("someJob"));
        assertThat(testee.getValue("someJob", "someKey"), is(nullValue()));
    }
}
