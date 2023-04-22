package de.malkusch.ha.automation.infrastructure.socket;

import java.io.IOException;

public interface Socket extends AutoCloseable {

    void turnOn() throws IOException;

    void turnOff() throws IOException;

    boolean isOn() throws IOException;
    
    boolean isOnline();

}
