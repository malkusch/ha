package de.malkusch.ha.automation.model.scooter;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatDuration;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import de.malkusch.ha.automation.model.electricity.Capacity;
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

        public boolean isCharging() throws IOException;

        public void stop();

        public boolean isOnline();
    }

    private volatile Instant startCoolDown = Instant.MIN;
    private volatile Instant stopCoolDown = Instant.MIN;

    public ScooterWallbox(Api api, Scooter scooter, Duration coolDown, Capacity balancingThreshold) throws IOException {
        this.api = api;
        this.scooter = scooter;

        this.coolDown = coolDown;
        log.info("Wallbox switch cool down is {}", formatDuration(coolDown));

        this.balancingThreshold = balancingThreshold;
        log.info("Wallbox balancing threshold is {}", balancingThreshold);

        if (api.isCharging()) {
            stopCoolDown = untilCoolDown();
            log.info("Wallbox started charging with stop cool down until {}", formatTime(stopCoolDown));

        } else {
            startCoolDown = untilCoolDown();
            log.info("Wallbox started not charging with start cool down until {}", formatTime(startCoolDown));
        }
    }

    @RequiredArgsConstructor
    public static class WallboxException extends Exception {

        public final Error error;

        public static enum Error {
            WALLBOX_OFFLINE, BATTERY_NOT_CONNECTED, SCOOTER_OFFLINE
        }

    }

    private static final WallboxException WALLBOX_OFFLINE = new WallboxException(Error.WALLBOX_OFFLINE);
    private static final WallboxException BATTERY_NOT_CONNECTED = new WallboxException(Error.BATTERY_NOT_CONNECTED);
    private static final WallboxException SCOOTER_OFFLINE = new WallboxException(Error.SCOOTER_OFFLINE);

    private void assertOnline() throws WallboxException {
        if (!api.isOnline()) {
            throw WALLBOX_OFFLINE;
        }
    }

    public void startCharging() throws IOException, WallboxException {
        assertOnline();

        if (!scooter.isOnline()) {
            throw SCOOTER_OFFLINE;
        }

        if (!scooter.isBatteryConnected()) {
            throw BATTERY_NOT_CONNECTED;
        }

        log.debug("Starting charging");
        if (api.isCharging()) {
            return;
        }

        if (Instant.now().isBefore(startCoolDown)) {
            log.debug("Can't start before {}", startCoolDown);
            return;
        }

        stopCoolDown = untilCoolDown();
        api.start();
        if (!api.isCharging()) {
            throw new IllegalStateException("Wallbox didn't start charging");
        }
        log.info("Charging started");
    }

    private Instant untilCoolDown() {
        return Instant.now().plus(coolDown);
    }

    public void stopCharging() throws IOException, WallboxException {
        assertOnline();

        log.debug("Stopping charging");
        if (!api.isCharging()) {
            return;
        }

        if (Instant.now().isBefore(stopCoolDown)) {
            log.debug("Can't stop before {}", stopCoolDown);
            return;
        }

        if (isBalancing()) {
            log.debug("Can't stop while balancing");
            return;
        }

        startCoolDown = untilCoolDown();
        api.stop();
        if (api.isCharging()) {
            throw new IllegalStateException("Wallbox didn't stop charging");
        }
        log.info("Charging stopped");
    }

    private boolean isBalancing() throws IOException {
        return scooter.charge().isGreaterThanOrEquals(balancingThreshold) && scooter.isCharging();
    }
}
