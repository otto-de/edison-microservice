package de.otto.edison.metrics.sender;

import com.codahale.metrics.graphite.GraphiteSender;
import org.junit.Test;

import java.io.IOException;
import java.util.function.Predicate;

import static de.otto.edison.metrics.sender.FilteringGraphiteSender.keepValuesByPattern;
import static de.otto.edison.metrics.sender.FilteringGraphiteSender.removePostfixValues;
import static java.util.regex.Pattern.compile;
import static org.mockito.Mockito.*;

public class FilteringGraphiteSenderTest {
    private final Long timestamp = 2L;
    private final String value = "45";
    private final Predicate<String> predicate = keepValuesByPattern(compile("anothermetric"));

    @Test
    public void shouldNotSendMetric() throws Exception {
        // given
        GraphiteSender delegate = mock(GraphiteSender.class);
        String name = "metrics.http.exception.p95";

        // when
        sendValue(name, delegate);

        // then
        verifyZeroInteractions(delegate);
    }

    @Test
    public void shouldSendMetric() throws Exception {
        // given
        GraphiteSender delegate = mock(GraphiteSender.class);
        String name = "metrics.anothermetric.min";

        // when
        sendValue(name, delegate);

        // then
        verify(delegate).send(name, value, timestamp);
        verifyNoMoreInteractions(delegate);
    }

    @Test
    public void shouldFilterDefaultValues() throws Exception {
        // given
        GraphiteSender delegate = mock(GraphiteSender.class);
        FilteringGraphiteSender graphiteSender = new FilteringGraphiteSender(delegate, removePostfixValues("abc"));
        String name = "holy.moly.string.ends.with.abc";

        // when
        graphiteSender.send(name, value, timestamp);

        // then
        verifyZeroInteractions(delegate);
    }

    private void sendValue(String name, GraphiteSender delegate) throws IOException {
        new FilteringGraphiteSender(delegate, predicate).send(name, value, timestamp);
    }
}