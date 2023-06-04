package de.malkusch.ha.automation.infrastructure.heater;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public final class TemporaryTemperatureService implements AutoCloseable {

    private final Heater heater;
    private volatile Temperature resetTemperature;

    TemporaryTemperatureService(KM200Heater heater) throws ApiException, InterruptedException {
        this.heater = heater;

        resetTemperature = heater.heaterTemperature();
        log.info("Reset temperature is {}", resetTemperature);
    }

    private volatile boolean changed = false;
    private final Object lock = new Object();

    public void changeTemporaryHeaterTemperature(Temperature temperature) throws ApiException, InterruptedException {
        synchronized (lock) {
            if (changed) {
                return;
            }
            log.info("Change temperature to {}", temperature);
            heater.changeHeaterTemperature(temperature);
            changed = true;
        }

    }

    public void resetTemporaryHeaterTemperature() throws ApiException, InterruptedException {
        synchronized (lock) {
            if (!changed) {
                return;
            }
            log.info("Reset temperature to {}", resetTemperature);
            heater.changeHeaterTemperature(resetTemperature);
            changed = false;
        }
    }

    @Scheduled(cron = "${buderus.heater.external-reset-temperature-check-cron}")
    void checkResetTemperatureForExternalChange() throws ApiException, InterruptedException {
        synchronized (lock) {
            if (changed) {
                return;
            }

            var temperature = heater.heaterTemperature();
            if (!temperature.equals(resetTemperature)) {
                log.warn("External temperature changed detected. Change reset temperature from {} to {}.",
                        resetTemperature, temperature);
                resetTemperature = temperature;
            }
        }
    }

    public boolean isChanged() {
        synchronized (lock) {
            return changed;
        }
    }

    public Temperature resetTemperature() {
        return resetTemperature;
    }

    public void close() throws Exception {
        resetTemporaryHeaterTemperature();
    }
}
