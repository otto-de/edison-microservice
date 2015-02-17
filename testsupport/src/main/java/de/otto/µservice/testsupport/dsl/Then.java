package de.otto.µservice.testsupport.dsl;

import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

public class Then {

    public static final Then INSTANCE = new Then();

    public static void then(final Then... thens) {}

    public static Then and(final Then then, final Then... more) { return Then.INSTANCE; }

    public static <T> Then assertThat(T actual, Matcher<? super T> matcher) {
        MatcherAssert.assertThat(actual, matcher);
        return INSTANCE;
    }

}
