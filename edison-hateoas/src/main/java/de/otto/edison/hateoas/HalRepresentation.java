package de.otto.edison.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.otto.edison.annotations.Beta;

import static de.otto.edison.hateoas.Embedded.withNothingEmbedded;
import static de.otto.edison.hateoas.Links.emptyLinks;

/**
 * Created by guido on 05.07.16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Beta
public class HalRepresentation {

    @JsonUnwrapped
    public final Links _links;
    @JsonUnwrapped
    public final Embedded _embedded;

    public HalRepresentation() {
        _links = emptyLinks();
        _embedded = withNothingEmbedded();
    }

    public HalRepresentation(final Links links) {
        _links = links;
        _embedded = withNothingEmbedded();
    }

    public HalRepresentation(final Links links, final Embedded embedded) {
        _links = links;
        _embedded = embedded;
    }

}
