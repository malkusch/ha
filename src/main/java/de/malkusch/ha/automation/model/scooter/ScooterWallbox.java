package de.malkusch.ha.automation.model.scooter;

import static de.malkusch.ha.automation.model.scooter.Scooter.State.READY_TO_CHARGE;

import java.io.IOException;

import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.geo.Location;
import de.malkusch.ha.automation.model.scooter.Scooter.ScooterException;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.WallboxException.Error;
import de.malkusch.ha.shared.infrastructure.CoolDown;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ScooterWallbox {

    private final Api api;

    public static interface Api {
        public void start() throws IOException;

        public void stop() throws IOException;

        public boolean isCharging() throws IOException;

        public boolean isOnline();
    }

    private final Scooter scooter;
    public final Location location;
    private final Capacity balancingThreshold;
    private final CoolDown coolDown;

    public ScooterWallbox(Location location, Api api, Scooter scooter, CoolDown coolDown, Capacity balancingThreshold)
            throws IOException {

        this.location = location;
        this.api = api;
        this.scooter = scooter;

        log.info("Wallbox switch cool down: {}", coolDown);
        this.coolDown = coolDown;

        log.info("Wallbox balancing threshold is {}", balancingThreshold);
        this.balancingThreshold = balancingThreshold;
    }

    public boolean isCharging() throws IOException, WallboxException {
        assertOnline();
        return api.isCharging();
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

        coolDown.withSilentCoolDown(() -> {
            log.debug("Start charging");
            api.start();
            if (!api.isCharging()) {
                throw new IllegalStateException("Wallbox didn't start charging");
            }
            log.info("Charging started");
        });
    }

    public void stopCharging() throws IOException, WallboxException {
        assertOnline();

        if (!api.isCharging()) {
            log.debug("Already stopped");
            return;
        }

        if (isBalancing()) {
            log.debug("Can't stop while balancing");
            return;
        }

        coolDown.withSilentCoolDown(() -> {
            log.debug("Stop charging");
            api.stop();
            if (api.isCharging()) {
                throw new IllegalStateException("Wallbox didn't stop charging");
            }
            log.info("Charging stopped");
        });
    }

    private boolean isBalancing() throws IOException {
        try {
            return scooter.charge().isGreaterThanOrEquals(balancingThreshold);

        } catch (ScooterException e) {
            log.warn("Can't get Scooter charge ({}), assuming no balancing", e.error);
            return false;
        }
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
