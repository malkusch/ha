package io.theves.denon4j;

import io.theves.denon4j.net.EventDispatcher;
import io.theves.denon4j.net.Protocol;
import io.theves.denon4j.net.Tcp;

public final class AccessibleDenonReceiver extends DenonReceiver {

    public static AccessibleDenonReceiver build(String host, Integer port) {
        var protocol = new Tcp(host, port);
        return new AccessibleDenonReceiver(protocol);
    }

    private final Protocol protocol;

    AccessibleDenonReceiver(Protocol protocol) {
        super(protocol);
        this.protocol = protocol;
    }

    public Protocol protocol() {
        return protocol;
    }

    public EventDispatcher eventDispatcher() {
        return getEventDispatcher();
    }
}