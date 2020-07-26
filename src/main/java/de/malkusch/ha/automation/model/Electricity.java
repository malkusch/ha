package de.malkusch.ha.automation.model;

import java.time.Duration;

import de.malkusch.ha.shared.model.ApiException;

public interface Electricity {

    public static enum Aggregation {
        MINIMUM, MAXIMUM
    }

    Watt excess(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

}
