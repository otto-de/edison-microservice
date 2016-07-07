package de.otto.edison.hateoas;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;

/**
 * Created by guido on 05.07.16.
 */
@JsonSerialize(using = Links.LinksSerializer.class)
public class Links {

    private static final Links EMPTY_LINKS = new Links(emptyMap());

    final Map<String, List<Link>> links;

    private Links(final Map<String, List<Link>> links) {
        this.links = links;
    }

    public static Links emptyLinks() {
        return EMPTY_LINKS;
    }

    public static Links linkingTo(final Link link, final Link... more) {
        final Map<String,List<Link>> allLinks = new LinkedHashMap<>();
        allLinks.put(link.rel, new ArrayList<Link>(){{add(link);}});
        if (more != null) {
            stream(more).forEach(l -> {
                if (!allLinks.containsKey(l.rel)) {
                    allLinks.put(l.rel, new ArrayList<>());
                }
                allLinks.get(l.rel).add(l);
            });
        }
        return new Links(allLinks);
    }

    static class LinksSerializer extends JsonSerializer<Links> {

        @Override
        @SuppressWarnings("unchecked")
        public void serialize(final Links value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeStartObject();
            for (final String rel : value.links.keySet()) {
                final List<Link> links = value.links.get(rel);
                if (links.size() > 1) {
                    gen.writeArrayFieldStart(rel);
                    for (final Link link : links) {
                        gen.writeObject(link);
                    }
                    gen.writeEndArray();
                } else {
                    gen.writeObjectField(rel, links.get(0));
                }
            }
            gen.writeEndObject();
        }
    }
}
