package de.otto.edison.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilTest {

    @Test
    void shouldReturnNullForNull() {
        assertNull(DateTimeUtil.parse(null));
    }

    @Test
    void shouldReturnNullForEmptyString() {
        assertNull(DateTimeUtil.parse(""));
    }

    @Test
    void shouldParseIsoOffsetWithColon() {
        OffsetDateTime result = DateTimeUtil.parse("2026-03-31T08:47:00+02:00");
        assertNotNull(result);
        assertEquals(ZoneOffset.of("+02:00"), result.getOffset());
    }

    @Test
    void shouldParseIsoOffsetWithoutColon() {
        // format produced by git-commit-id-plugin
        OffsetDateTime result = DateTimeUtil.parse("2026-03-31T08:47+0200");
        assertNotNull(result);
        assertEquals(ZoneOffset.of("+02:00"), result.getOffset());
    }

    @Test
    void shouldParseUtcZ() {
        OffsetDateTime result = DateTimeUtil.parse("2026-03-31T06:56:18.632572089Z");
        assertNotNull(result);
        assertEquals(ZoneOffset.UTC, result.getOffset());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "2026-03-31T06:56:18.632572089Z",
            "2026-03-31T08:47+0200",
            "2026-03-31T08:47:00+02:00",
            "2026-03-31T08:47:00Z"
    })
    void shouldParseAllKnownFormats(String input) {
        assertNotNull(DateTimeUtil.parse(input));
    }
}
