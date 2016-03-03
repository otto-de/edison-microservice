package de.otto.edison.status.indicator.load;

public class EverythingFineStrategy implements OverloadDetector {

    @Override
    public boolean isOverloaded() {
        return false;
    }
}
