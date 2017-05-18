package de.otto.edison.metrics.sender;

import com.codahale.metrics.graphite.GraphiteSender;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FilteringGraphiteSender implements GraphiteSender {
    private static final Predicate<String> PREDICATE_ALL = (a) -> true;

    private final GraphiteSender delegate;
    private final Predicate<String> predicate;

    public FilteringGraphiteSender(GraphiteSender delegate, Predicate<String> predicate) {
        this.delegate = delegate;
        this.predicate = predicate;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        delegate.connect();
    }

    public static Predicate<String> keepAll() {
        return PREDICATE_ALL;
    }

    public static Predicate<String> keepValuesByPattern(Pattern pattern) {
        return pattern.asPredicate();
    }

    public static Predicate<String> keepValuesByPatterns(Stream<Pattern> pattern) {
        return pattern
                .map(Pattern::asPredicate)
                .reduce(keepAll(), Predicate::or);
    }

    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        if (predicate.test(name)) {
            delegate.send(name, value, timestamp);
        }
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public int getFailures() {
        return delegate.getFailures();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public static Predicate<String> removePostfixValues(String ... postfixValues) {
        return keepValuesByPatterns(Arrays.stream(postfixValues)
                .map(Pattern::quote)
                .map(s -> s+"$")
                .map(Pattern::compile))
                .negate();
    }
}
