package de.malkusch.ha.automation.model.weather;

import java.time.LocalDate;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.shared.model.ApiException;

public interface Weather {

    Cloudiness averageDaylightCloudiness() throws ApiException, InterruptedException;

    Cloudiness averageDaylightCloudiness(LocalDate date) throws ApiException, InterruptedException;

    Temperature temperature() throws ApiException, InterruptedException;

    Temperature highestDailyTemperature() throws ApiException, InterruptedException;

    WindSpeed windspeed() throws ApiException, InterruptedException;
}
