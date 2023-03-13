package de.malkusch.ha.automation.model.electricity;

import java.time.Duration;
import java.time.LocalDate;

import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Wallbox {

    private final Watt minimumLoadingConsumption;
    private final Duration minimumLoadingDuration;
    private final Electricity electricity;

    public boolean isLoadingWhileProducingElectricity(LocalDate date) throws ApiException, InterruptedException {
        return electricity.isConsumptionDuringProductionGreaterThan(date, minimumLoadingConsumption, minimumLoadingDuration);
    }
}
