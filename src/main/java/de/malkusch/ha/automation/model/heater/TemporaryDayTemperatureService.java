package de.malkusch.ha.automation.model.heater;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class TemporaryDayTemperatureService implements AutoCloseable {

    private final Heater heater;
    private final int steps;
    private final Temperature step;

    private int offset = 0;
    private final Object lock = new Object();

    public void reset() throws ApiException, InterruptedException {
        synchronized (lock) {
            offset = 0;
            heater.resetTemporaryHeaterTemperatur();
        }
    }

    public void stepDown() throws ApiException, InterruptedException {
        synchronized (lock) {
            if (offset <= -steps) {
                return;
            }
            offset--;
            log.info("Stepping heater down to {} steps ", offset);
            heater.changeTemporaryHeaterTemperatur(offsetTemperature());
        }
    }

    public void stepUp() throws ApiException, InterruptedException {
        synchronized (lock) {
            if (offset >= steps) {
                return;
            }
            offset++;
            log.info("Stepping heater up to {} steps ", offset);
            heater.changeTemporaryHeaterTemperatur(offsetTemperature());
        }
    }

    public void stepMin() throws ApiException, InterruptedException {
        synchronized (lock) {
            if (offset <= -steps) {
                return;
            }
            offset = -steps;
            log.info("Stepping heater to minimum");
            heater.changeTemporaryHeaterTemperatur(offsetTemperature());
        }
    }

    private Temperature offsetTemperature() throws ApiException, InterruptedException {
        return heater.dayTemperature().plus(step.multiply(offset));
    }

    @Override
    public void close() throws Exception {
        reset();
    }
}
