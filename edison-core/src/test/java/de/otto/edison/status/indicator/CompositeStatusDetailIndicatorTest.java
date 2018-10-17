package de.otto.edison.status.indicator;

import de.otto.edison.status.domain.StatusDetail;
import org.junit.jupiter.api.Test;

import static de.otto.edison.status.domain.Status.ERROR;
import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.StatusDetail.statusDetail;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompositeStatusDetailIndicatorTest {

    @Test
    public void shouldNotAcceptEmptyListOfDelegates() {

        assertThrows(IllegalArgumentException.class, () -> {
            new CompositeStatusDetailIndicator(emptyList());
        });
    }

    @Test
    public void shouldReturnStatusDetailOfSingleIndicator() {
        final StatusDetailIndicator indicator = new MutableStatusDetailIndicator(statusDetail("bar", ERROR, "a message"));
        final CompositeStatusDetailIndicator composite = new CompositeStatusDetailIndicator(asList(indicator));
        final StatusDetail statusDetail = composite.statusDetails().get(0);
        assertThat(statusDetail, is(statusDetail("bar", ERROR, "a message")));
    }

    @Test
    public void shouldAggregateStatusDetails() {
        final StatusDetailIndicator first = new MutableStatusDetailIndicator(statusDetail("firstIndicator", OK, "a message"));
        final StatusDetailIndicator second = new MutableStatusDetailIndicator(statusDetail("secondIndicator", ERROR, "a message"));
        final CompositeStatusDetailIndicator composite = new CompositeStatusDetailIndicator(asList(first, second));
        assertThat(composite.statusDetails().get(0), is(statusDetail("firstIndicator", OK, "a message")));
        assertThat(composite.statusDetails().get(1), is(statusDetail("secondIndicator", ERROR, "a message")));
    }

    @Test
    public void shouldProvideStatusDetails() {
        final StatusDetailIndicator first = new MutableStatusDetailIndicator(statusDetail("firstIndicator", OK, "a message"));
        final StatusDetailIndicator second = new MutableStatusDetailIndicator(statusDetail("secondIndicator", ERROR, "a message"));
        final CompositeStatusDetailIndicator composite = new CompositeStatusDetailIndicator(asList(first, second));
        assertThat(composite.statusDetails(), contains(
                statusDetail("firstIndicator", OK, "a message"),
                statusDetail("secondIndicator", ERROR, "a message"))
        );
    }

}