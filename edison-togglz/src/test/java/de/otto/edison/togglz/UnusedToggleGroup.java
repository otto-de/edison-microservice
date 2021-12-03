package de.otto.edison.togglz;

import org.togglz.core.annotation.FeatureGroup;
import org.togglz.core.annotation.Label;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@FeatureGroup
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Label("UnusedToggleGroup") //necessary as togglz 3.0.0 leads to 'Class' group representation
public @interface UnusedToggleGroup {
}
