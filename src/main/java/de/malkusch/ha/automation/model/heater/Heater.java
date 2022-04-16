package de.malkusch.ha.automation.model.heater;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.shared.model.ApiException;

public interface Heater {

    public boolean isHeating() throws ApiException, InterruptedException;

    public void changeHeaterTemperature(Temperature temperature) throws ApiException, InterruptedException;

    public Temperature heaterTemperature() throws ApiException, InterruptedException;
}
