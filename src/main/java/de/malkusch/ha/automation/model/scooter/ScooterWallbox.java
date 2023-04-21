package de.malkusch.ha.automation.model.scooter;

import static de.malkusch.ha.automation.model.scooter.Scooter.State.READY_TO_CHARGE;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatDuration;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;
import static java.time.Instant.now;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.scooter.Scooter.ScooterException;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.WallboxException.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ScooterWallbox {

    private final Api api;
    private final Scooter scooter;
    private final Duration coolDown;
    private final Capacity balancingThreshold;

    public static interface Api {
        public void start();

        public void stop();

        public boolean isCharging() throws IOException;

        public boolean isOnline();
    }

    private volatile Instant startCoolDown;
    private volatile Instant stopCoolDown;

    public ScooterWallbox(Api api, Scooter scooter, Duration coolDown, Capacity balancingThreshold) throws IOException {
        this.api = api;
        this.scooter = scooter;

        log.info("Wallbox switch cool down is {}", formatDuration(coolDown));
        this.coolDown = coolDown;
        startCoolDown = newCoolDown();
        stopCoolDown = newCoolDown();

        log.info("Wallbox balancing threshold is {}", balancingThreshold);
        this.balancingThreshold = balancingThreshold;
    }

    public void startCharging() throws IOException, WallboxException {
        assertOnline();

        if (scooter.state() != READY_TO_CHARGE) {
            throw SCOOTER_NOT_READY_TO_CHARGE;
        }

        if (api.isCharging()) {
            log.debug("Already charging");
            return;
        }

        if (now().isBefore(startCoolDown)) {
            log.debug("Start cool down until {}", formatTime(startCoolDown));
            return;
        }

        log.debug("Start charging");
        stopCoolDown = newCoolDown();
        api.start();
        if (!api.isCharging()) {
            throw new IllegalStateException("Wallbox didn't start charging");
        }
        log.info("Charging started");
    }

    public void stopCharging() throws IOException, WallboxException {
        assertOnline();

        if (!api.isCharging()) {
            log.debug("Already stopped");
            return;
        }

        if (now().isBefore(stopCoolDown)) {
            log.debug("Stop cool down until {}", formatTime(stopCoolDown));
            return;
        }

        if (isBalancing()) {
            log.debug("Can't stop while balancing");
            return;
        }

        log.debug("Stop charging");
        startCoolDown = newCoolDown();
        api.stop();
        if (api.isCharging()) {
            throw new IllegalStateException("Wallbox didn't stop charging");
        }
        log.info("Charging stopped");
    }

    private boolean isBalancing() throws IOException {
        try {
            return scooter.charge().isGreaterThanOrEquals(balancingThreshold);

        } catch (ScooterException e) {
            log.warn("Can't get Scooter charge ({}), assuming no balancing", e.error, e);
            return false;
        }
    }

    private Instant newCoolDown() {
        return now().plus(coolDown);
    }

    @RequiredArgsConstructor
    public static class WallboxException extends Exception {
        private static final long serialVersionUID = 5845898922927989990L;

        public final Error error;

        public static enum Error {
            WALLBOX_OFFLINE, BATTERY_NOT_CONNECTED, SCOOTER_NOT_READY_TO_CHARGE
        }

    }

    private static final WallboxException WALLBOX_OFFLINE = new WallboxException(Error.WALLBOX_OFFLINE);
    private static final WallboxException SCOOTER_NOT_READY_TO_CHARGE = new WallboxException(
            Error.SCOOTER_NOT_READY_TO_CHARGE);

    private void assertOnline() throws WallboxException {
        if (!api.isOnline()) {
            throw WALLBOX_OFFLINE;
        }
    }
}
