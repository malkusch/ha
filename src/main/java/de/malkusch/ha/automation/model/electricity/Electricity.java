package de.malkusch.ha.automation.model.electricity;

import java.time.Duration;
import java.time.LocalDate;

import de.malkusch.ha.shared.model.ApiException;

public interface Electricity {

    public static enum Aggregation {
        MINIMUM, P25, MAXIMUM, P75
    }

    Watt excess(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

    Watt excess() throws ApiException, InterruptedException;

    Watt excessProduction(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

    Watt excessProduction() throws ApiException, InterruptedException;

    Watt consumption(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

    Watt production(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

    Watt batteryConsumption(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException;

    Capacity capacity() throws ApiException, InterruptedException;

    boolean wasFullyCharged(LocalDate date) throws ApiException, InterruptedException;
}
