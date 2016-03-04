package de.otto.edison.status.indicator.load;

import de.otto.edison.annotations.Beta;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Beta
public class EverythingFineStrategy implements LoadDetector {

    private static final Logger LOG = getLogger(EverythingFineStrategy.class);

    public EverythingFineStrategy() {
        LOG.info("Initialize 'everything fine' strategy");
    }

    @Override
    public Status getStatus() {
        return Status.BALANCED;
    }
}
