package de.malkusch.ha.automation.model;

import java.time.Duration;

import de.malkusch.ha.shared.model.ApiException;

public interface Electricity {

    public static enum Aggregation {
        MINIMUM, P25, MAXIMUM, P75
    }

    Watt excess(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

    Watt consumption(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

    Watt production(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

    Watt batteryConsumption(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

    Capacity capacity() throws ApiException, InterruptedException;
}
