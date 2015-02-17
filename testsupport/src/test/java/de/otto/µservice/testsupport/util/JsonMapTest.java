package de.otto.µservice.testsupport.util;


import org.testng.annotations.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static de.otto.µservice.testsupport.util.JsonMap.jsonMapFrom;
import static java.time.Instant.now;
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

    // -------- String -------------

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
    public void shouldAutomaticallyApplyToStringOnDouble() {
        Map<String, ?> map = singletonMap("key", 2.0);
        assertThat(jsonMapFrom(map).getString("key"), is("2.0"));
    }


    @Test
    public void shouldFallbackOnDefaultDouble() {
        JsonMap empty = jsonMapFrom(new HashMap<String, Object>());
        assertThat(empty.getDouble("key", 2.0), is(2.0));
    }


    // ----------- boolean ------------

    @Test
    public void shouldGetBoolean() {
        Map<String, ?> map = singletonMap("key", Boolean.TRUE);
        assertThat(jsonMapFrom(map).getBoolean("key"), is(Boolean.TRUE));
    }

    @Test
    public void shouldGetBooleanFromString() {
        Map<String, ?> map = singletonMap("key", "true");
        assertThat(jsonMapFrom(map).getBoolean("key"), is(Boolean.TRUE));
    }

    @Test
    public void shouldAutomaticallyApplyToStringOnBoolean() {
        Map<String, ?> map = singletonMap("key", Boolean.TRUE);
        assertThat(jsonMapFrom(map).getString("key"), is("true"));
    }


    @Test
    public void shouldFallbackOnDefaultBoolean() {
        JsonMap empty = jsonMapFrom(new HashMap<String, Object>());
        assertThat(empty.getBoolean("key", Boolean.TRUE), is(Boolean.TRUE));
    }


    // -------------- date ------------------
    @Test
    public void shouldGetDate() {
        Date date = new Date();
        Map<String, ?> map = singletonMap("key", date);
        assertThat(jsonMapFrom(map).getDate("key"), is(date));
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void shouldThrowClassCastExceptionIfStringInsteadOfDate() {
        Map<String, ?> map = singletonMap("key", "Tue Jul 13 00:00:00 CEST 1999");
        jsonMapFrom(map).getDate("key");
    }

    @Test
    public void shouldAutomaticallyApplyToStringOnDate() throws ParseException {
        Map<String, Object> map = new HashMap<>();
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("dd-MM-yyyy", Locale.GERMAN);
        Date date = simpleDateFormat.parse("13-07-1999");
        map.put("key", date);
        assertThat(jsonMapFrom(map).getString("key"), startsWith("Tue Jul 13 00:00:00"));
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
    public void shouldAutomaticallyApplyToStringOnInt() {
        Map<String, ?> map = singletonMap("key", 1);
        assertThat(jsonMapFrom(map).getString("key"), is("1"));
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
    public void shouldAutomaticallyApplyToStringOnLong() {
        Map<String, Long> map = singletonMap("key", 1L);
        assertThat(jsonMapFrom(map).getString("key"), is("1"));
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
