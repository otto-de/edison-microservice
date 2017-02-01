package de.otto.edison.mongo.jobs;

import com.github.fakemongo.Fongo;
import com.mongodb.client.MongoDatabase;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MongoJobStateRepositoryTest {

    MongoJobStateRepository testee;

    @Before
    public void setup() {
        final MongoDatabase database = new Fongo("inMemoryDb").getDatabase("jobstate");
        testee = new MongoJobStateRepository(database);
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