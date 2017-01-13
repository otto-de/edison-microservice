package de.otto.edison.testsupport.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Sets {

    @SafeVarargs
    public static <T> Set<T> hashSet(T... values) {
        Set<T> result = new HashSet<>();
        if(values==null) {
            return result;
        }
        Collections.addAll(result, values);
        return result;
    }
}
