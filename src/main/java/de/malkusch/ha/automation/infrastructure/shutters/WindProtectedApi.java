package de.malkusch.ha.automation.infrastructure.shutters;

import static java.util.Objects.requireNonNull;

import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.automation.model.weather.WindSpeed;
import de.malkusch.ha.automation.model.weather.WindSpeedChanged;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class WindProtectedApi implements Api {

    private final WindSpeed releaseThreshold;
    private final WindSpeed protectThreshold;
    private final State protection;
    private final Api api;

    private final Object lock = new Object();
    private boolean windProtected = false;
    private State desired;

    public WindProtectedApi(WindSpeed releaseThreshold, WindSpeed protectThreshold, State protection, Api api)
            throws ApiException, InterruptedException {

        this.releaseThreshold = requireNonNull(releaseThreshold);
        this.protectThreshold = requireNonNull(protectThreshold);
        this.protection = requireNonNull(protection);
        this.api = requireNonNull(api);

        if (!releaseThreshold.isLessThan(protectThreshold)) {
            throw new IllegalArgumentException("releaseThreshold must be less than protectThreshold");
        }

        synchronized (lock) {
            desired = api.state();
        }
    }

    @Override
    public void setState(State state) throws ApiException, InterruptedException {
        synchronized (lock) {
            desired = state;
            if (!windProtected) {
                api.setState(state);
            }
        }
    }

    @Override
    public State state() throws ApiException, InterruptedException {
        synchronized (lock) {
            if (windProtected) {
                return desired;
            }
            return api.state();
        }
    }

    public void onWindSpeedChanged(WindSpeedChanged event) throws ApiException, InterruptedException {
        if (event.windSpeed().isGreaterThan(protectThreshold)) {
            windProtect();
        } else if (event.windSpeed().isLessThan(releaseThreshold)) {
            release();
        }
    }

    private void windProtect() throws ApiException, InterruptedException {
        synchronized (lock) {
            if (windProtected) {
                return;
            }
            log.info("Protecting from wind");
            desired = api.state();
            api.setState(protection);
            windProtected = true;
        }
    }

    private void release() throws ApiException, InterruptedException {
        synchronized (lock) {
            if (!windProtected) {
                return;
            }
            log.info("Releasing wind protection");
            api.setState(desired);
            windProtected = false;
        }
    }
}
