package de.otto.µservice.testsupport.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Optional;

public class OptionalMatchers {

    public static Matcher<? super Optional<?>> isPresent() {
        return new BaseMatcher<Optional<?>>() {

            @Override
            public boolean matches(Object item) {
                return Optional.class.isAssignableFrom(item.getClass())
                        && ((Optional)item).isPresent();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Optional should be present");
            }
        };
    }

    public static Matcher<? super Optional<?>> isAbsent() {
        return new BaseMatcher<Optional<?>>() {

            @Override
            public boolean matches(Object item) {
                return Optional.class.isAssignableFrom(item.getClass())
                        && !((Optional)item).isPresent();
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Optional should be absent");
            }
        };
    }

}
