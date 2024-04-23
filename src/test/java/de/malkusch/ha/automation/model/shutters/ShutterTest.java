package de.malkusch.ha.automation.model.shutters;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;
import static de.malkusch.ha.automation.model.shutters.ShutterId.TERRASSE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import de.malkusch.ha.automation.model.astronomy.Azimuth;
import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.automation.model.shutters.Shutter.Api.State;
import de.malkusch.ha.automation.model.shutters.Shutter.LockedException;
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
        shutter = new Shutter(TERRASSE, api, Duration.ofNanos(1),
                new DirectSunLightRange(new Azimuth(10), new Azimuth(20)));
        shutter.open();
    }

    private final static Duration ANY_LOCK_DURATION = Duration.ofSeconds(1);

    public static State[] ALL_STATES() {
        return new State[] { OPEN, CLOSED };
    }

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
    public void lockShouldFail_whenLockedDifferently() throws Exception {
        shutter.lock(OPEN, ANY_LOCK_DURATION);

        assertThrows(LockedException.class, () -> shutter.lock(CLOSED, ANY_LOCK_DURATION));
    }

    @Test
    public void lockShouldNotFail_whenLockedIdentically() throws Exception {
        shutter.lock(OPEN, ANY_LOCK_DURATION);

        assertThrows(LockedException.class, () -> shutter.lock(OPEN, ANY_LOCK_DURATION));
    }

    @ParameterizedTest
    @MethodSource("ALL_STATES")
    public void unlockShouldRestoreOldStateWhenUnlocked(State lockedState) throws Exception {
        shutter.open();
        shutter.lock(lockedState, ANY_LOCK_DURATION);

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

    @ParameterizedTest
    @MethodSource("ALL_STATES")
    public void WhenStateChangedDuringLock_lockShouldChangeBackIntoLockedState(State lockedState) throws Exception {
        shutter.lock(CLOSED, ANY_LOCK_DURATION);
        api.setState(OPEN); // e.g. using the physical switch

        shutter.lock(lockedState, ANY_LOCK_DURATION);

        assertEquals(lockedState, api.state());
        assertTrue(shutter.isLocked());
    }

    @ParameterizedTest
    @MethodSource("ALL_STATES")
    public void WhenStateChangedDuringLock_shutterIsUnlocked(State changeUnlocked) throws Exception {
        shutter.open();
        shutter.lock(CLOSED, ANY_LOCK_DURATION);

        api.setState(OPEN); // e.g. using the physical switch

        changeState(changeUnlocked);
        assertEquals(changeUnlocked, api.state());
        assertFalse(shutter.isLocked());
    }

    @Test
    public void WhenStateChangedDuringLock_unlockShouldShouldNotRestoreDesiredState() throws Exception {
        shutter.close();
        shutter.lock(CLOSED, ANY_LOCK_DURATION);
        api.setState(OPEN); // e.g. using the physical switch

        shutter.unlock();

        assertEquals(OPEN, api.state());
    }

    @Test
    public void WhenPhysicallyOpened_closeShouldClose() throws Exception {
        shutter.close();
        api.setState(OPEN); // e.g. using the physical switch

        shutter.close();

        assertEquals(CLOSED, api.state());
    }

    @Test
    public void forceOpenShouldOpenWhenLocked() throws Exception {
        shutter.close();
        shutter.lock(CLOSED, ANY_LOCK_DURATION);

        shutter.forceOpen();

        assertEquals(OPEN, api.state());
    }
    
    @Test
    public void forceOpenShouldOpenWhenClosed() throws Exception {
        shutter.close();
        
        shutter.forceOpen();
        
        assertEquals(OPEN, api.state());
    }

    private static Api.State state(String state) {
        return switch (state) {
        case "OPEN" -> OPEN;
        case "CLOSED" -> CLOSED;
        default -> throw new IllegalArgumentException(state);
        };
    }

    private void changeState(String state) throws ApiException, InterruptedException {
        changeState(state(state));
    }

    private void changeState(State state) throws ApiException, InterruptedException {
        if (state.equals(OPEN)) {
            shutter.open();
        } else if (state.equals(CLOSED)) {
            shutter.close();
        }
    }
}
