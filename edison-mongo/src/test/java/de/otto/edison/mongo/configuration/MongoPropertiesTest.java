package de.otto.edison.mongo.configuration;


import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MongoPropertiesTest {

    @Test
    public void shouldReturnPassword() {
        final MongoProperties props = new MongoProperties();
        props.setPassword("somePassword");
        assertThat(props.getPassword(), is("somePassword"));
    }

}
