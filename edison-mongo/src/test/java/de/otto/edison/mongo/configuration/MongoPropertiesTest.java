package de.otto.edison.mongo.configuration;

import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCompressor;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

public class MongoPropertiesTest {

    @Test
    public void shouldReturnPassword() {
        final MongoProperties props = new MongoProperties();
        props.setPassword("somePassword");
        assertThat(props.getPassword(), is("somePassword"));
    }

    @Test
    void shouldConfigureCompressorListWhenClientServerCompressionIsEnabled() {
        //given
        final MongoProperties props = new MongoProperties();

        //when
        props.setClientServerCompressionEnabled(true);
        MongoClientSettings mongoClientOptions = props.toMongoClientSettings(MongoClientSettings.getDefaultCodecRegistry(), Collections.singletonList(MongoCompressor.createZlibCompressor()), null);

        //then
        assertThat(mongoClientOptions.getCompressorList(), is(Collections.singletonList(MongoCompressor.createZlibCompressor())));
    }

    @Test
    void shouldNotConfigureCompressorListWhenClientServerCompressionIsDisabled() {
        //given
        final MongoProperties props = new MongoProperties();

        //when
        props.setClientServerCompressionEnabled(false);
        MongoClientSettings mongoClientOptions = props.toMongoClientSettings(MongoClientSettings.getDefaultCodecRegistry(), Collections.singletonList(MongoCompressor.createZlibCompressor()), null);

        //then
        assertThat(mongoClientOptions.getCompressorList(), is(Collections.emptyList()));
    }

    @Test
    void shouldUseConnectionStringWhenUriPatternWithAwsIamIsProvided() {
        //given
        final MongoProperties props = new MongoProperties();
        props.setUriPattern("mongodb+srv://cluster0.x4oml.mongodb.net/myFirstDatabase?authSource=%24external&authMechanism=MONGODB-AWS");

        //when
        MongoClientSettings mongoClientOptions = props.toMongoClientSettings(MongoClientSettings.getDefaultCodecRegistry(), null, null);

        //then
        assertThat(mongoClientOptions.getCredential(), is(notNullValue()));
        assertThat(mongoClientOptions.getCredential().getAuthenticationMechanism(), is(AuthenticationMechanism.MONGODB_AWS));
        assertThat(mongoClientOptions.getCredential().getSource(), is("$external"));
    }

    @Test
    void shouldUseConnectionStringWhenUriPatternWithScramIsProvided() {
        //given
        final MongoProperties props = new MongoProperties();
        props.setUser("aUsername");
        props.setPassword("somePassword");
        props.setUriPattern("mongodb+srv://%s:%s@cluster0.x4oml.mongodb.net/myFirstDatabase");

        //when
        MongoClientSettings mongoClientOptions = props.toMongoClientSettings(MongoClientSettings.getDefaultCodecRegistry(), null, null);

        //then
        assertThat(mongoClientOptions.getCredential(), is(notNullValue()));
        assertThat(mongoClientOptions.getCredential().getUserName(), is("aUsername"));
        assertThat(mongoClientOptions.getCredential().getPassword(), is("somePassword".toCharArray()));
        assertThat(mongoClientOptions.getCredential().getSource(), is("admin"));
    }
}
