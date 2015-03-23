package de.otto.edison.status.indicator;


import java.net.InetAddress;
import java.net.UnknownHostException;

public class DefaultHostnameIndicator implements HostnameIndicator {

    @Override
    public String hostname() {
        try {
            InetAddress localhost = java.net.InetAddress.getLocalHost();
            return localhost.getHostName();
        } catch (UnknownHostException e) {
            return "UNKOWN";
        }
    }
}
