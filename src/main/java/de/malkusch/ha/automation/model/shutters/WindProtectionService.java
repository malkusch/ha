package de.malkusch.ha.automation.model.shutters;

import static java.util.Objects.requireNonNull;

import java.time.Duration;

import de.malkusch.ha.automation.model.shutters.Shutter.Api.State;
import de.malkusch.ha.automation.model.shutters.Shutter.LockedException;
import de.malkusch.ha.automation.model.weather.WindSpeed;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WindProtectionService<T extends Shutter> {

    private final WindSpeed releaseThreshold;
    private final WindSpeed protectThreshold;
    private final State protection;
    private final Duration lockDuration;

    public WindProtectionService(WindSpeed releaseThreshold, WindSpeed protectThreshold, State protection,
            Duration lockDuration) {
        this.releaseThreshold = requireNonNull(releaseThreshold);
        this.protectThreshold = requireNonNull(protectThreshold);
        this.protection = requireNonNull(protection);
        this.lockDuration = requireNonNull(lockDuration);

        if (!releaseThreshold.isLessThan(protectThreshold)) {
            throw new IllegalArgumentException("releaseThreshold must be less than protectThreshold");
        }
    }

    public void checkProtection(T shutter, WindSpeed windSpeed) throws ApiException, InterruptedException {
        if (windSpeed.isGreaterThan(protectThreshold)) {
            try {
                log.info("Protecting {} against too much wind ({})", shutter, windSpeed);
                shutter.lock(protection, lockDuration);

            } catch (LockedException e) {
                log.info("{} is already locked", shutter);
            }
        } else if (windSpeed.isLessThan(releaseThreshold)) {
            shutter.unlock();
        }
    }
}
