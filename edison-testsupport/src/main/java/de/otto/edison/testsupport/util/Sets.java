package de.otto.edison.testsupport.util;

import java.util.HashSet;

public final class Sets {

    public static <T> HashSet<T> hashSet(T... values) {
        HashSet<T> result = new HashSet<>();
        if(values==null) {
            return result;
        }
        for(T value: values) {
            result.add(value);
        }
        return result;
    }
}
