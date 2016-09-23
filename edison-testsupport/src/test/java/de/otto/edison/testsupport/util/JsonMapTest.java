package de.otto.edison.testsupport.util;



import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static de.otto.edison.testsupport.util.JsonMap.jsonMapFrom;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.StringStartsWith.startsWith;

public class JsonMapTest {

    @Test
    public void shouldGetInnerMap() {
        Map<String, ?> inner = singletonMap("key", "value");
        Map<String, ?> map = singletonMap("inner", inner);
        assertThat(jsonMapFrom(map).get("inner"), is(jsonMapFrom(inner)));
    }

    @Test
    public void shouldGetInnerValueByDottedKeys() {
        /**
         * {
         * "outer" : {
         *      "innter": {
         *          "key" : "value"
         *      }
         * }
         */
        Map<String, ?> inner = singletonMap("key", "value");
        Map<String, ?> map = singletonMap("outer", singletonMap("inner", inner));
        assertThat(jsonMapFrom(map).get("outer.inner").getString("key"), is("value"));
    }

    // -------- remove -------------

    @Test
    public void shouldRemoveKeyFromTopLevel() {
        Map<String, ?> map = new HashMap<String,String>(){{put("removeMe", "hello world");}};
        JsonMap jsonMap = jsonMapFrom(map);
        jsonMap.remove("removeMe");
        assertThat(jsonMap, is(jsonMapFrom(emptyMap())));
    }

    @Test
    public void shouldRemoveKeyFromInnerObject() {
        Map<String, ?> map = singletonMap("key", new HashMap<String,String>(){{put("removeMe", "hello world");}});
        final JsonMap jsonMap = jsonMapFrom(map);
        jsonMap.get("key").remove("removeMe");
        assertThat(jsonMap, is(jsonMapFrom(singletonMap("key", emptyMap()))));
    }

    // -------- getString -------------

    @Test
    public void shouldGetNullForMissingString() {
        Map<String, ?> map = singletonMap("key", null);
        assertThat(jsonMapFrom(map).getString("key"), is(nullValue()));
        assertThat(jsonMapFrom(map).getString("doesnotexist"), is(nullValue()));
    }

    @Test
    public void shouldGetString() {
        Map<String, ?> map = singletonMap("key", "value");
        assertThat(jsonMapFrom(map).getString("key"), is("value"));
    }

    @Test
    public void shouldFallbackOnDefaultString() {
        JsonMap empty = jsonMapFrom(new HashMap<String, Object>());
        assertThat(empty.getString("key", "default"), is("default"));
    }

    @Test
    public void shouldGetStringFromDouble() {
        Map<String, ?> map = singletonMap("key", 2.0d);
        assertThat(jsonMapFrom(map).getString("key"), is("2.0"));
    }

    @Test
    public void shouldGetStringFromLong() {
        Map<String, ?> map = singletonMap("key", 2L);
        assertThat(jsonMapFrom(map).getString("key"), is("2"));
    }

    @Test
    public void shouldGetStringFromInteger() {
        Map<String, ?> map = singletonMap("key", 1);
        assertThat(jsonMapFrom(map).getString("key"), is("1"));
    }

    @Test
    public void shouldGetStringFromBoolean() {
        Map<String, ?> map = new HashMap<String,Boolean>() {{
            put("somethingTrue", TRUE);
            put("somethingFalse", FALSE);
        }};
        assertThat(jsonMapFrom(map).getString("somethingTrue"), is("true"));
        assertThat(jsonMapFrom(map).getString("somethingFalse"), is("false"));
    }

    // --------------- double ---------------
    @Test
    public void shouldGetDouble() {
        Map<String, ?> map = singletonMap("key", 2.0);
        assertThat(jsonMapFrom(map).getDouble("key"), is(2.0));
    }

    @Test
    public void shouldGetDoubleFromString() {
        Map<String, ?> map = singletonMap("key", "2");
        assertThat(jsonMapFrom(map).getDouble("key"), is(2.0));
    }

    @Test
    public void shouldFallbackOnDefaultDouble() {
        JsonMap empty = jsonMapFrom(new HashMap<String, Object>());
        assertThat(empty.getDouble("key", 2.0), is(2.0));
    }


    // ----------- boolean ------------

    @Test
    public void shouldGetBoolean() {
        Map<String, ?> map = singletonMap("key", TRUE);
        assertThat(jsonMapFrom(map).getBoolean("key"), is(TRUE));
    }

    @Test
    public void shouldGetBooleanFromString() {
        Map<String, ?> map = singletonMap("key", "true");
        assertThat(jsonMapFrom(map).getBoolean("key"), is(TRUE));
    }

    @Test
    public void shouldFallbackOnDefaultBoolean() {
        JsonMap empty = jsonMapFrom(new HashMap<String, Object>());
        assertThat(empty.getBoolean("key", TRUE), is(TRUE));
    }


    // -------------- date ------------------
    @Test
    public void shouldGetDate() {
        Date date = new Date();
        Map<String, ?> map = singletonMap("key", date);
        assertThat(jsonMapFrom(map).getDate("key"), is(date));
    }

    @Test
    public void shouldGetDateFromProperStringFormat() {
        // 2012-04-23T18:25:43.511Z ISO 8601
        Map<String, ?> map = singletonMap("key", "2012-04-23T18:25:43.511Z");
        assertThat(jsonMapFrom(map).getDate("key"), is(Date.from(LocalDateTime.of(2012, 4, 23, 18, 25, 43, 511000000).toInstant(UTC))));
    }

    @Test(expected = DateTimeParseException.class)
    public void shouldThrowExceptionWhenGettingDateFromWrongStringFormat() {
        Map<String, ?> map = singletonMap("key", "Tue Jul 13 00:00:00");
        jsonMapFrom(map).getDate("key");
    }

    // -------------- int ------------------
    @Test
    public void shouldGetInt() {
        Map<String, ?> map = singletonMap("key", 1);
        assertThat(jsonMapFrom(map).getInt("key"), is(1));
    }

    @Test
    public void shouldGetIntFromString() {
        Map<String, ?> map = singletonMap("key", "1");
        assertThat(jsonMapFrom(map).getInt("key"), is(1));
    }

    @Test
    public void shouldFallbackOnDefaultInt() {
        JsonMap empty = jsonMapFrom(new HashMap<String, Object>());
        assertThat(empty.getInt("key", 1), is(1));
    }


    // -------------- long ------------------
    @Test
    public void shouldGetLong() {
        Map<String, ?> map = singletonMap("key", 1L);
        assertThat(jsonMapFrom(map).getLong("key"), is(1L));
    }

    @Test
    public void shouldGetLongFromString() {
        Map<String, ?> map = singletonMap("key", "1");
        assertThat(jsonMapFrom(map).getLong("key"), is(1L));
    }

    @Test
    public void shouldFallbackOnDefaultLong() {
        JsonMap empty = jsonMapFrom(new HashMap<String, Object>());
        assertThat(empty.getLong("key", 1L), is(1L));
    }

    @Test
    public void shouldReturnNullIfLongValueIsNotPresentAndNoDefaultSpecified() {
        JsonMap empty = jsonMapFrom(new HashMap<String, Object>());
        assertThat(empty.getLong("key"), is(nullValue()));
    }

    // --- Instant

    @Test
    public void shouldReturnNullIfInstantValueIsNotPresentAndNoDefaultSpecified() {
        JsonMap empty = jsonMapFrom(new HashMap<String, Object>());
        assertThat(empty.getInstant("key"), is(nullValue()));
    }

    @Test
    public void shouldGetInstant() {
        Instant instant = now();
        Map<String, ?> map = singletonMap("key", instant.toString());
        assertThat(jsonMapFrom(map).getInstant("key"), is(instant));
    }
}