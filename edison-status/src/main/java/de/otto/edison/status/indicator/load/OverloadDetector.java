package de.otto.edison.status.indicator.load;

import de.otto.edison.status.domain.Status;

/**
 * Implementation of this interface allow to steer whether the application
 * is able to run smoothly versus nearly unable to handle the load and
 * therefore escalates it as a {@link Status#WARNING}.
 */
public interface OverloadDetector {

    boolean isOverloaded();

}
