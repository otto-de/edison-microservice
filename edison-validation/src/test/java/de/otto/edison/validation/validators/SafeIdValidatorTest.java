package de.otto.edison.validation.validators;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class SafeIdValidatorTest {

    @Parameterized.Parameter(0)
    public String inputId;

    @Parameterized.Parameter(1)
    public boolean expectedValid;

    private SafeIdValidator validator = new SafeIdValidator();

    @Test
    public void testAllExampleIdValidPairs() {
        assertThat(validator.isValid(inputId, null), is(expectedValid));
    }


    @Parameterized.Parameters(name = "{index}: Is {0} valid? => {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[] { "id",                                              true },
                new Object[] { "long-id",                                         true },
                new Object[] { "long-id-with-numbers-1234567890",                 true },
                new Object[] { "long-id-with-numbers-1234567890-and-underscore_", true },
                new Object[] { "ID-with-CAPITALS",                                true },
                new Object[] { null,                                              true },
                new Object[] { "id<",                                             false },
                new Object[] { "id>",                                             false },
                new Object[] { "id!",                                             false },
                new Object[] { "id@",                                             false },
                new Object[] { "id#",                                             false },
                new Object[] { "id$",                                             false },
                new Object[] { "id%",                                             false },
                new Object[] { "id^",                                             false },
                new Object[] { "id&",                                             false },
                new Object[] { "id*",                                             false },
                new Object[] { "id(",                                             false },
                new Object[] { "id)",                                             false },
                new Object[] { "id+",                                             false },
                new Object[] { "id=",                                             false }
        );
    }

}
