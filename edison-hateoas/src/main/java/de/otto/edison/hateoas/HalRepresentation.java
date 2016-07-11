package de.otto.edison.hateoas;

import com.fasterxml.jackson.annotation.*;
import de.otto.edison.annotations.Beta;

import static de.otto.edison.hateoas.Embedded.emptyEmbedded;
import static de.otto.edison.hateoas.Links.emptyLinks;

/**
 *
 * @see <a href="http://stateless.co/hal_specification.html"></a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Beta
public class HalRepresentation {

    @JsonProperty(value = "_links")
    final Links links;
    @JsonProperty(value = "_embedded")
    final Embedded embedded;

    public HalRepresentation() {
        this.links = null;
        embedded = null;
    }

    public HalRepresentation(final Links links) {
        this.links = links;
        embedded = null;
    }

    public HalRepresentation(final Links links, final Embedded embedded) {
        this.links = links;
        this.embedded = embedded;
    }

    @JsonIgnore
    public Links getLinks() {
        return links != null ? links : emptyLinks();
    }

    @JsonIgnore
    public Embedded getEmbedded() {
        return embedded != null ? embedded : emptyEmbedded();
    }
}
