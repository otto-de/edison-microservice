package de.otto.edison.aws.s3.togglz;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

public enum TestFeatures implements Feature {

    @Label("a first test feature toggle")
    TEST_FEATURE_1;
}
