package de.otto.edison.mongo.configuration;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class MongoPropertiesTest {

    @Test
    public void shouldReturnPasswordWhenDeprecatedPasswordFieldIsUsed() throws Exception {
        MongoProperties props = new MongoProperties();
        props.setPasswd("somePassword");
        assertThat(props.getPassword(), is("somePassword"));
    }

    @Test
    public void shouldReturnPasswordWhenNewPasswordFieldIsUsed() throws Exception {
        MongoProperties props = new MongoProperties();
        props.setPassword("somePassword");
        assertThat(props.getPassword(), is("somePassword"));
    }

    @Test
    public void shouldPreferNewPasswordOverDeprecatedOne() throws Exception {
        MongoProperties props = new MongoProperties();
        props.setPasswd("someDeprecatedPassword");
        props.setPassword("someNewPassword");
        assertThat(props.getPassword(), is("someNewPassword"));
    }

}
