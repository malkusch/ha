package de.malkusch.ha.automation.model.scooter;

import java.io.IOException;

import de.malkusch.ha.automation.model.electricity.Capacity;

public interface Scooter {

    public Capacity charge() throws IOException;
    
    public boolean isCharging() throws IOException;
    
}
