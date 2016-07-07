package de.otto.edison.hateoas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.util.List;

import static de.otto.edison.hateoas.Embedded.withEmbedded;
import static de.otto.edison.hateoas.Link.link;
import static de.otto.edison.hateoas.Link.self;
import static de.otto.edison.hateoas.Links.linkingTo;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Created by guido on 05.07.16.
 */
public class HalRepresentationEmbeddingTest {

    @Test
    public void shouldRenderEmbeddedResourcesWithProperties() throws JsonProcessingException {
        // given
        final List<HalRepresentation> items = asList(
                new HalRepresentation(linkingTo(self("http://example.org/test/bar/01"))) {public String amount="42€";},
                new HalRepresentation(linkingTo(self("http://example.org/test/bar/02"))) {public String amount="4711€";}
        );
        final HalRepresentation representation = new HalRepresentation(
                linkingTo(self("http://example.org/test/bar")),
                withEmbedded("orders", items)) {public String total="4753€";};
        // when
        final String json = new ObjectMapper().writeValueAsString(representation);
        // then
        assertThat(json, is(
                "{" +
                        "\"_links\":{\"self\":{\"href\":\"http://example.org/test/bar\"}}," +
                        "\"_embedded\":{\"orders\":[" +
                                "{" +
                                    "\"_links\":{\"self\":{\"href\":\"http://example.org/test/bar/01\"}},\"amount\":\"42€\"" +
                                "}," +
                                "{" +
                                    "\"_links\":{\"self\":{\"href\":\"http://example.org/test/bar/02\"}},\"amount\":\"4711€\"" +
                                "}" +
                                "]}," +
                        "\"total\":\"4753€\"" +
                "}"));
    }

    @Test
    public void shouldRenderEmbeddedResourcesWithMultipleLinks() throws JsonProcessingException {
        // given
        final List<HalRepresentation> items = asList(
                new HalRepresentation(linkingTo(
                        link("test", "http://example.org/test/bar/01"),
                        link("test", "http://example.org/test/bar/02"))) {public String amount="42€";}
        );
        final HalRepresentation representation = new HalRepresentation(
                linkingTo(self("http://example.org/test/bar")),
                withEmbedded("orders", items)) {public String total="4753€";};
        // when
        final String json = new ObjectMapper().writeValueAsString(representation);
        // then
        assertThat(json, is(
                "{" +
                        "\"_links\":{\"self\":{\"href\":\"http://example.org/test/bar\"}}," +
                        "\"_embedded\":{\"orders\":[" +
                                "{" +
                                    "\"_links\":{\"test\":[{\"href\":\"http://example.org/test/bar/01\"},{\"href\":\"http://example.org/test/bar/02\"}]},\"amount\":\"42€\"" +
                                "}" +
                                "]}," +
                        "\"total\":\"4753€\"" +
                "}"));
    }

    @Test
    public void shouldRenderMultipleEmbeddedResources() throws JsonProcessingException {
        // given
        final HalRepresentation representation = new HalRepresentation(
                linkingTo(self("http://example.org/test/bar")),
                withEmbedded()
                        .itemsWithRel("foo", asList(
                                new HalRepresentation(linkingTo(self("http://example.org/test/foo/01"))),
                                new HalRepresentation(linkingTo(self("http://example.org/test/foo/02")))
                        ))
                        .itemsWithRel("bar", asList(
                                new HalRepresentation(linkingTo(self("http://example.org/test/bar/01"))),
                                new HalRepresentation(linkingTo(self("http://example.org/test/bar/02")))
                        )).build());
        // when
        final String json = new ObjectMapper().writeValueAsString(representation);
        // then
        assertThat(json, is(
                "{" +
                        "\"_links\":{\"self\":{\"href\":\"http://example.org/test/bar\"}}," +
                        "\"_embedded\":{" +
                            "\"foo\":[{" +
                                    "\"_links\":{\"self\":{\"href\":\"http://example.org/test/foo/01\"}}" +
                                "},{" +
                                    "\"_links\":{\"self\":{\"href\":\"http://example.org/test/foo/02\"}}" +
                                "}]," +
                            "\"bar\":[{" +
                                    "\"_links\":{\"self\":{\"href\":\"http://example.org/test/bar/01\"}}" +
                                "},{" +
                                    "\"_links\":{\"self\":{\"href\":\"http://example.org/test/bar/02\"}}" +
                                "}]" +
                        "}" +
                "}"));
    }

}
