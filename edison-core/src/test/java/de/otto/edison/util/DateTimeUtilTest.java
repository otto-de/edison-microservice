package de.otto.edison.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DateTimeUtilTest {

    @Test
    void shouldReturnNullForNull() {
        assertNull(DateTimeUtil.parse(null));
    }

    @Test
    void shouldReturnNullForEmptyString() {
        assertNull(DateTimeUtil.parse(""));
    }

    static Stream<Arguments> knownFormats() {
        return Stream.of(
                arguments(
                        "nanosecond precision with UTC Z",
                        "2026-03-31T06:56:18.632572089Z",
                        OffsetDateTime.of(2026, 3, 31, 6, 56, 18, 632572089, ZoneOffset.UTC)
                ),
                arguments(
                        "git-commit-id-plugin format without colon in offset",
                        "2026-03-31T08:47+0200",
                        OffsetDateTime.of(2026, 3, 31, 8, 47, 0, 0, ZoneOffset.of("+02:00"))
                ),
                arguments(
                        "ISO-8601 with colon in offset",
                        "2026-03-31T08:47:00+02:00",
                        OffsetDateTime.of(2026, 3, 31, 8, 47, 0, 0, ZoneOffset.of("+02:00"))
                ),
                arguments(
                        "ISO-8601 with UTC Z",
                        "2026-03-31T08:47:00Z",
                        OffsetDateTime.of(2026, 3, 31, 8, 47, 0, 0, ZoneOffset.UTC)
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("knownFormats")
    void shouldParseKnownFormats(String description, String input, OffsetDateTime expected) {
        assertEquals(expected, DateTimeUtil.parse(input));
    }
}
