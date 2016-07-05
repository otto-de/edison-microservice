package de.otto.edison.hateoas;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;

/**
 * Created by guido on 05.07.16.
 */
@JsonInclude(NON_EMPTY)
public class Links {

    private static final Links EMPTY_LINKS = new Links(emptyMap());

    public final Map<String, Link> _links;

    private Links(final Map<String, Link> links) {
        _links = links;
    }

    public static Links emptyLinks() {
        return EMPTY_LINKS;
    }

    public static Links linkingTo(final Link link, final Link... more) {
        return new Links(new LinkedHashMap<String, Link>() {{
            put(link.rel, link);
            if (more != null) {
                stream(more).forEach(l -> put(l.rel, l));
            }
        }});
    }
}
