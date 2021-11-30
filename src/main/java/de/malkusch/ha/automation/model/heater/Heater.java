package de.malkusch.ha.automation.model.heater;

import de.malkusch.ha.shared.model.ApiException;

public interface Heater {

    public boolean isHeating() throws ApiException, InterruptedException;
}
