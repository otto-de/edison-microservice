package de.otto.edison.status.domain;

import net.jcip.annotations.Immutable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

@Immutable
public class SystemInfo {

    private final String hostName;
    private final int port;

    private SystemInfo(final String hostName, final int port) {
        this.hostName = hostName;
        this.port = port;
    }

    public static SystemInfo systemInfo(final String hostName, final int port) {
        return new SystemInfo(hostName, port);
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SystemInfo that = (SystemInfo) o;

        if (port != that.port) return false;
        return !(hostName != null ? !hostName.equals(that.hostName) : that.hostName != null);

    }

    @Override
    public int hashCode() {
        int result = hostName != null ? hostName.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "hostName='" + hostName + '\'' +
                ", port=" + port +
                '}';
    }
}
