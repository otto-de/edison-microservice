package de.otto.edison.togglz.repository.mongo;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

public enum TestFeatures implements Feature {

    @Label("a first test feature toggle")
    TEST_FEATURE_1,

    @Label("a second test feature toggle")
    TEST_FEATURE_2;
}
