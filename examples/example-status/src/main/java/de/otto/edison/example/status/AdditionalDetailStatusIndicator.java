package de.otto.edison.example.status;

import de.otto.edison.status.domain.StatusDetail;
import de.otto.edison.status.indicator.StatusDetailIndicator;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

import static de.otto.edison.status.domain.Status.OK;

/**
 * StatusDetails may contain additional information about the status.
 *
 * @author Guido Steinacker
 * @since 13.02.15
 */
@Component
public class AdditionalDetailStatusIndicator implements StatusDetailIndicator {

    @Override
    public StatusDetail statusDetail() {
        return StatusDetail.statusDetail("Status with additional details", OK, "Some message",
                new LinkedHashMap<String,String>() {{
                    put("first", "extra information");
                    put("second", "more information");
                    put("third", "even more information");
                }});
    }

}
