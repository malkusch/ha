package de.malkusch.ha.automation.model.scooter;

import static de.malkusch.ha.automation.model.scooter.Scooter.State.READY_TO_CHARGE;

import java.io.IOException;

import de.malkusch.ha.automation.model.geo.Location;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.WallboxException.Error;
import de.malkusch.ha.shared.infrastructure.CoolDown;
import de.malkusch.ha.shared.infrastructure.CoolDown.CoolDownException;
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
    private final CoolDown coolDown;

    public ScooterWallbox(Location location, Api api, Scooter scooter, CoolDown coolDown) throws IOException {

        this.location = location;
        this.api = api;
        this.scooter = scooter;

        log.info("Wallbox switch cool down: {}", coolDown);
        this.coolDown = coolDown;
    }

    public boolean isCharging() throws IOException, WallboxException {
        assertOnline();
        return api.isCharging();
    }

    public void startCharging() throws IOException, WallboxException, CoolDownException {
        assertOnline();

        if (scooter.state() != READY_TO_CHARGE) {
            throw SCOOTER_NOT_READY_TO_CHARGE;
        }

        if (api.isCharging()) {
            log.debug("Already charging");
            return;
        }

        coolDown.withCoolDown(() -> {
            log.debug("Start charging");
            api.start();
            if (!api.isCharging()) {
                throw new IllegalStateException("Wallbox didn't start charging");
            }
            log.info("Charging started");
        });
    }

    public void stopCharging() throws IOException, WallboxException, CoolDownException {
        assertOnline();

        if (!api.isCharging()) {
            log.debug("Already stopped");
            return;
        }

        coolDown.withCoolDown(() -> {
            log.debug("Stop charging");
            api.stop();
            if (api.isCharging()) {
                throw new IllegalStateException("Wallbox didn't stop charging");
            }
            log.info("Charging stopped");
        });
    }

    public static class WallboxException extends Exception {
        private static final long serialVersionUID = 5845898922927989990L;

        public final Error error;

        public static enum Error {
            WALLBOX_OFFLINE, BATTERY_NOT_CONNECTED, SCOOTER_NOT_READY_TO_CHARGE, CANT_STOP_BALANCING
        }

        WallboxException(Error error) {
            super(error.toString());
            this.error = error;
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
