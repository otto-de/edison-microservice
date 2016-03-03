package de.otto.edison.status.indicator.load;

public class EverythingFineStrategy implements LoadDetector {

    @Override
    public Status getStatus() {
        return Status.BALANCED;
    }
}
