package de.malkusch.ha.automation.infrastructure.heater;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class TemporaryTemperatureService implements AutoCloseable {

    private final Heater heater;
    private final Temperature resetTemperature;

    TemporaryTemperatureService(KM200Heater heater) throws ApiException, InterruptedException {
        this.heater = heater;

        resetTemperature = heater.heaterTemperature();
        log.info("Reset temperature is {}", resetTemperature);
    }

    public void changeTemporaryHeaterTemperature(Temperature temperature) throws ApiException, InterruptedException {
        heater.changeHeaterTemperature(temperature);
    }

    public void resetTemporaryHeaterTemperature() throws ApiException, InterruptedException {
        heater.changeHeaterTemperature(resetTemperature);
    }

    public void close() throws Exception {
        resetTemporaryHeaterTemperature();
    }
}
