package de.malkusch.ha.automation.infrastructure.socket;

import java.io.IOException;

public interface Socket extends AutoCloseable {

    void turnOn();

    void turnOff();

    boolean isOn() throws IOException;

}
