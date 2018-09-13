package de.otto.edison.oauth;

import com.fasterxml.jackson.core.JsonParser;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ZonedDateTimeDeserializerTest {

    @Mock
    private JsonParser jsonParser;

    private ZonedDateTimeDeserializer zonedDateTimeDeserializer;

    @Before
    public void setUp() {
        initMocks(this);
        zonedDateTimeDeserializer = new ZonedDateTimeDeserializer();
    }

    @Test
    public void shouldDeserializeZonedDateTime() throws IOException {
        // given
        when(jsonParser.getText()).thenReturn("2017-10-19T16:10:00.000+02:00[Europe/Berlin]");
        final ZonedDateTime expectedDateTime = ZonedDateTime.of(2017,10,19,16,10,0,0, ZoneId.of("Europe/Berlin"));

        // when
        final ZonedDateTime deserialized = zonedDateTimeDeserializer.deserialize(jsonParser, null);

        // then
        assertThat(deserialized, is(expectedDateTime));
    }
}