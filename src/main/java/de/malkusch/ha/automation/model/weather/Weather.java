package de.malkusch.ha.automation.model.weather;

import java.time.LocalDate;

import de.malkusch.ha.shared.model.ApiException;

public interface Weather {

    Cloudiness cloudiness(LocalDate date) throws ApiException, InterruptedException;

}
