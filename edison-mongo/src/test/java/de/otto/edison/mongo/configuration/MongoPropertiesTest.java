package de.otto.edison.mongo.configuration;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MongoPropertiesTest {

    @Test
    public void shouldReturnPassword() {
        MongoProperties props = new MongoProperties();
        props.setPassword("somePassword");
        assertThat(props.getPassword(), is("somePassword"));
    }

}
