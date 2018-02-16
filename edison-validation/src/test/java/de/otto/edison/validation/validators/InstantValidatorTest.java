package de.otto.edison.validation.validators;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class InstantValidatorTest {

    @Test
    public void shouldReturnFalseIfInstantNotParseable() throws Exception {
        // given
        InstantValidator subject = new InstantValidator();
        // when
        boolean result = subject.isValid("blabla", null);
        //then
        assertThat(result, is(false));
    }

    @Test
    public void shouldReturnTrueIfInstantIsParseable() throws Exception {
        // given
        InstantValidator subject = new InstantValidator();
        // when
        boolean result = subject.isValid("2042-02-05T10:17:38.858Z", null);
        //then
        assertThat(result, is(true));
    }
}