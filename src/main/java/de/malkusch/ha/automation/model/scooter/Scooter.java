package de.malkusch.ha.automation.model.scooter;

import static de.malkusch.ha.automation.model.scooter.Scooter.State.BATTERY_DISCONNECTED;
import static de.malkusch.ha.automation.model.scooter.Scooter.State.OFFLINE;

import java.io.IOException;

import de.malkusch.ha.automation.model.electricity.Capacity;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Scooter {

    private final Api api;

    public interface Api {
        Capacity charge() throws IOException;

        State state() throws IOException;
    }

    public static enum State {
        OFFLINE, BATTERY_DISCONNECTED, READY_TO_CHARGE, CHARGING,
    }

    public State state() throws IOException {
        return api.state();
    }

    public Capacity charge() throws IOException, ScooterException {
        var state = state();
        assertOnline(state);
        assertBatteryConnected(state);

        return api.charge();
    }

    private static final ScooterException SCOOTER_OFFLINE = new ScooterException(
            ScooterException.Error.SCOOTER_OFFLINE);

    private static void assertOnline(State state) throws ScooterException {
        if (state == OFFLINE) {
            throw SCOOTER_OFFLINE;
        }
    }

    private static final ScooterException BATTERY_NOT_CONNECTED = new ScooterException(
            ScooterException.Error.BATTERY_NOT_CONNECTED);

    private static void assertBatteryConnected(State state) throws ScooterException {
        if (state == BATTERY_DISCONNECTED) {
            throw BATTERY_NOT_CONNECTED;
        }
    }

    @RequiredArgsConstructor
    public static class ScooterException extends Exception {

        public final Error error;

        public static enum Error {
            SCOOTER_OFFLINE, BATTERY_NOT_CONNECTED
        }
    }
}
