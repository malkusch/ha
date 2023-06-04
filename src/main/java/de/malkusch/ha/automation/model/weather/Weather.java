package de.malkusch.ha.automation.model.weather;

import java.time.Instant;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.shared.model.ApiException;

public interface Weather {

    Temperature highestDailyTemperature() throws ApiException, InterruptedException;

    WindSpeed windspeed() throws ApiException, InterruptedException;
    
    Instant lastUpdate();
}
