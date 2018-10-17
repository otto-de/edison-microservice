package de.otto.edison.validation.validators;

import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class InstantValidatorTest {

    @Test
    public void shouldReturnFalseIfInstantIsNotParseable() {
        // given
        final InstantValidator subject = new InstantValidator();
        // when
        final boolean result = subject.isValid("blabla", null);
        //then
        assertThat(result, is(false));
    }

    @Test
    public void shouldReturnTrueIfInstantIsParseable() {
        // given
        final InstantValidator subject = new InstantValidator();
        // when
        final boolean result = subject.isValid("2042-02-05T10:17:38.858Z", null);
        //then
        assertThat(result, is(true));
    }

    @Test
    public void shouldReturnTrueIfInstantIsNull() {
        // given
        final InstantValidator subject = new InstantValidator();
        // when
        final boolean result = subject.isValid(null, null);
        //then
        assertThat(result, is(true));

    }
}