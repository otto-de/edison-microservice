package de.otto.edison.testsupport.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.addAll;

public final class Sets {

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Set<T> hashSet(T... values) {
        Set<T> result = new HashSet<>();
        if(values==null) {
            return result;
        }
        result.addAll(asList(values));
        return result;
    }
}
