package de.otto.edison.jobs.definition;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static de.otto.edison.jobs.definition.DefaultJobDefinition.validateCron;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class DefaultJobDefinitionTest {

    @Test
    public void shouldNotFailOnValidCron() {
        validateCron("* * * * * *");
        validateCron("* 1,2,3 * * * *");
        validateCron("* * * * * ?");
        validateCron("* * * * * 0");
        validateCron("* * 21 * * *");
    }

    @Test
    public void shouldFailOnInvalidInput() {
        checkFailure("");
        checkFailure(" ");
        checkFailure("* * * * * * *");
        checkFailure("99 0 0 0 0 0 ");
        checkFailure("* * * * *");
        checkFailure("46-66 0 0 0 * *");
    }

    private void checkFailure(String cron) {
        try {
            validateCron(cron);
            fail("'" + cron + "' should yield an IllegalArgumentException");
        } catch (Exception e) {
            assertThat(e, Matchers.instanceOf(IllegalArgumentException.class));
        }
    }
}
