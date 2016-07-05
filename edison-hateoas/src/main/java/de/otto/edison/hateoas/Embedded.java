package de.otto.edison.hateoas;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Collections.singletonMap;

/**
 * Created by guido on 05.07.16.
 */
@JsonInclude(NON_NULL)
public class Embedded {

    public final Map<String,List<HalRepresentation>> _embedded;

    private Embedded(final Map<String, List<HalRepresentation>> embedded) {
        _embedded = embedded;
    }

    public static Embedded withNothingEmbedded() {
        return new Embedded(null);
    }

    public static Embedded withEmbedded(final String rel,
                                        final List<HalRepresentation> embeddedRepresentations) {
        return new Embedded(singletonMap(rel, embeddedRepresentations));
    }

    public static EmbeddedItemsBuilder withEmbedded() {
        return new EmbeddedItemsBuilder();
    }

    public final static class EmbeddedItemsBuilder {
        public final Map<String,List<HalRepresentation>> _embedded = new LinkedHashMap<>();

        public EmbeddedItemsBuilder itemsWithRel(final String rel, final List<HalRepresentation> embeddedRepresentations) {
            _embedded.put(rel, embeddedRepresentations);
            return this;
        }

        public Embedded build() {
            return _embedded.isEmpty() ? withNothingEmbedded() : new Embedded(_embedded);
        }
    }
}
