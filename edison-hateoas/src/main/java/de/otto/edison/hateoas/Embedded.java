package de.otto.edison.hateoas;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;

/**
 * Created by guido on 05.07.16.
 */
@JsonSerialize(using = Embedded.EmbeddedSerializer.class)
@JsonDeserialize(using = Embedded.EmbeddedDeserializer.class)
public class Embedded {

    //@JsonProperty(value = "_embedded")
    private final Map<String,List<HalRepresentation>> items;

    Embedded() {
        items =null;}

    private Embedded(final Map<String, List<HalRepresentation>> items) {
        this.items = items;
    }

    public static Embedded emptyEmbedded() {
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
            return _embedded.isEmpty() ? emptyEmbedded() : new Embedded(_embedded);
        }
    }

    @JsonIgnore
    public List<HalRepresentation> getItemsBy(final String rel) {
        return items.containsKey(rel) ? items.get(rel) : emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Embedded embedded = (Embedded) o;

        return this.items != null ? this.items.equals(embedded.items) : embedded.items == null;

    }

    @Override
    public int hashCode() {
        return items != null ? items.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Embedded{" +
                "items=" + items +
                '}';
    }

    static class EmbeddedSerializer extends JsonSerializer<Embedded> {

        @Override
        public void serialize(Embedded value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeObject(value.items);
        }
    }

    static class EmbeddedDeserializer extends JsonDeserializer<Embedded> {

        private static final TypeReference<Map<String, List<HalRepresentation>>> TYPE_REF_LIST_OF_HAL_REPRESENTATIONS = new TypeReference<Map<String, List<HalRepresentation>>>() {};

        @Override
        public Embedded deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final Map<String,List<HalRepresentation>> items = p.readValueAs(TYPE_REF_LIST_OF_HAL_REPRESENTATIONS);
            return new Embedded(items);
        }
    }
}
