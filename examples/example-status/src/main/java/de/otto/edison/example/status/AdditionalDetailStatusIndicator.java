package de.otto.edison.example.status;

import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static de.otto.edison.status.domain.Status.OK;
import static de.otto.edison.status.domain.StatusDetail.*;
import static java.util.Collections.*;

/**
 * StatusDetails may contain additional information about the status.
 *
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Component
public class AdditionalDetailStatusIndicator implements StatusDetailIndicator {

    @Override
    public List<StatusDetail> statusDetails() {
        return singletonList(statusDetail("Status with additional details", OK, "Some message",
                new LinkedHashMap<String, String>() {{
                    put("first", "extra information");
                    put("second", "more information");
                    put("third", "even more information");
                }}));
    }

}
