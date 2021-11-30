package de.malkusch.ha.automation.model;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;
import static de.malkusch.ha.automation.model.shutters.ShutterId.TERRASSE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.malkusch.ha.automation.model.shutters.Shutter;
import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.shared.model.ApiException;

public class ShutterTest {

    private Api api = new MockApi();

    private static class MockApi implements Api {
        private State state = OPEN;

        @Override
        public void setState(State state) throws ApiException, InterruptedException {
            this.state = state;
        }

        @Override
        public State state() throws ApiException, InterruptedException {
            return state;
        }
    }

    private Shutter shutter;

    @BeforeEach
    public void setUpShutter() throws Exception {
        shutter = new Shutter(TERRASSE, api, Duration.ofNanos(1));
        shutter.open();
    }

    private final static Duration ANY_LOCK_DURATION = Duration.ofSeconds(1);

    @Test
    public void openShouldNotChangeStateWhenLocked() throws Exception {
        shutter.lock(CLOSED, ANY_LOCK_DURATION);

        shutter.open();

        assertEquals(CLOSED, api.state());
    }

    @Test
    public void closeShouldNotChangeStateWhenLocked() throws Exception {
        shutter.lock(OPEN, ANY_LOCK_DURATION);

        shutter.close();

        assertEquals(OPEN, api.state());
    }

    @Test
    public void unlockShouldRestoreOldStateWhenUnlocked() throws Exception {
        shutter.open();
        shutter.lock(CLOSED, ANY_LOCK_DURATION);

        shutter.unlock();

        assertEquals(OPEN, api.state());
    }

    @ParameterizedTest
    @CsvSource(value = { //
            "OPEN,  OPEN,  OPEN,  OPEN", //
            "OPEN,  OPEN,CLOSED,CLOSED", //
            "OPEN,CLOSED,  OPEN,  OPEN", //
            "OPEN,CLOSED,CLOSED,CLOSED", //

            "CLOSED,  OPEN,  OPEN,  OPEN", //
            "CLOSED,  OPEN,CLOSED,CLOSED", //
            "CLOSED,CLOSED,  OPEN,  OPEN", //
            "CLOSED,CLOSED,CLOSED,CLOSED", //
    })
    public void unlockShouldRestoreChangedDesiredStateWhenUnlocked(String initState, String lockState,
            String desiredState, String expectedState) throws Exception {

        changeState(initState);
        shutter.lock(state(lockState), ANY_LOCK_DURATION);

        changeState(desiredState);

        shutter.unlock();

        assertEquals(state(expectedState), api.state());
    }

    public void unlockShouldRestoreLatestDesiredStateWhenUnlocked() throws Exception {
        shutter.open();
        shutter.lock(CLOSED, ANY_LOCK_DURATION);

        shutter.close();
        shutter.open();

        shutter.unlock();

        assertEquals(OPEN, api.state());
    }

    @Test
    public void lockShouldRestoreLockedStateWhenStateChangeDuringLock() {

    }

    private static Api.State state(String state) {
        return switch (state) {
        case "OPEN": {
            yield OPEN;
        }
        case "CLOSED": {
            yield CLOSED;
        }
        default:
            throw new IllegalArgumentException(state);
        };
    }

    private void changeState(String state) throws ApiException, InterruptedException {
        if (state(state).equals(OPEN)) {
            shutter.open();
        } else if (state(state).equals(CLOSED)) {
            shutter.close();
        }
    }
}
