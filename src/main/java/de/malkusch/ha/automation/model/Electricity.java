package de.malkusch.ha.automation.model;

import java.time.Duration;

public interface Electricity {

    public static enum Aggregation {
        MINIMUM, MAXIMUM
    }

    Watt excess(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

}
