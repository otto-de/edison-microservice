package de.otto.edison.dependencies.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.List;
import java.util.Objects;

import static java.lang.Integer.valueOf;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@JsonSerialize(using = ToStringSerializer.class)
public final class Datasource {

    /**
     * The node (host name) of the datasource.
     */
    public final String node;
    /**
     * The port used to access the datasource. If the port is -1, no port is configured.
     */
    public final int port;
    /**
     * The Datasource resource accessed by the service.
     *
     * <p>
     * Examples are Database, Collection, Topic...
     * </p>
     */
    public final String resource;

    private Datasource(final String node,
                       final int port,
                       final String resource) {
        this.node = node;
        this.port = port;
        this.resource = resource;
    }

    /**
     * Parses the comma-separated datasources string into a list of Datasource objects.
     * <p>
     *     The format is &lt;node&gt;:&lt;port&gt;/&lt;resource&gt;,&lt;node&gt;:&lt;port&gt;/&lt;resource&gt;, ...
     * </p>
     *
     * @param ds comma-separated list of datasources
     * @return list of Datasources
     */
    public static List<Datasource> datasources(final String ds) {
        if (ds.contains(",")) {
            return stream(ds.split(",")).map(Datasource::datasource).collect(toList());
        } else {
            return singletonList(datasource(ds));
        }
    }

    /**
     * Parses the datasource string into node, port and resource and returns a Datasource instance for these parts.
     * <p>
     *     The format is &lt;node&gt;:&lt;port&gt;/&lt;resource&gt;
     * </p>
     *
     * @param ds datasource string
     * @return Datasource
     */
    @JsonCreator
    public static Datasource datasource(final String ds) {
        String s = ds;
        String resource = "";
        if (ds.contains("/")) {
            final String[] serverAndResource = ds.split("/");
            s = serverAndResource[0];
            resource = serverAndResource[1];
        }
        if (ds.contains(":")) {
            String[] nodeAndPort = s.split(":");
            return datasource(nodeAndPort[0], valueOf(nodeAndPort[1]), resource);
        }
        return datasource(s, -1, resource);
    }

    /**
     * Creates a Datasource from node, port and resource descriptors.
     *
     * @param node the node of the datasource
     * @param port the port used to access the datasource
     * @param resource the resource accessed at the datasource
     *
     * @return Datasource
     */
    public static Datasource datasource(final String node,
                                        final int port,
                                        final String resource) {
        return new Datasource(node, port, resource);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Datasource)) return false;
        Datasource that = (Datasource) o;
        return port == that.port &&
                Objects.equals(node, that.node) &&
                Objects.equals(resource, that.resource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, port, resource);
    }

    @Override
    public String toString() {
        return node + ':' + port + "/" + resource;
    }
}
