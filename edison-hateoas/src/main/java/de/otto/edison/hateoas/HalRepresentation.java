package de.otto.edison.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.annotations.Beta;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.stream;

/**
 * Created by guido on 05.07.16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Beta
public class HalRepresentation {

    public final Map<String,Link> _links;

    public HalRepresentation() {
        _links = new LinkedHashMap<>();
    }

    public HalRepresentation(final Link link, final Link... more) {
        _links = new LinkedHashMap<String,Link>() {{
            put(link.rel, link);
            if (more != null) {
                stream(more).forEach(l->put(l.rel, l));
            }
        }};
    }

}
