package de.malkusch.ha.automation.model.heater;

import org.springframework.stereotype.Service;

import de.malkusch.ha.shared.model.ApiException;

@Service
public final class TemporayHotWaterTemperatureService implements AutoCloseable {

    private final Temperature defaultTemperature;
    private final Heater heater;

    TemporayHotWaterTemperatureService(Heater heater) throws ApiException, InterruptedException {
        this.heater = heater;
        defaultTemperature = heater.hotwaterHighTemperature();
    }

    public void reduceBy(Temperature delta) throws ApiException, InterruptedException {
        heater.changeHotwaterHighTemperature(defaultTemperature.minus(delta));
    }

    public void reset() throws ApiException, InterruptedException {
        heater.changeHotwaterHighTemperature(defaultTemperature);
    }

    @Override
    public void close() throws Exception {
        reset();
    }
}
