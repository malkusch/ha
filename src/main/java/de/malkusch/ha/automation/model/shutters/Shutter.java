package de.malkusch.ha.automation.model.shutters;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.CLOSED;
import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;
import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.time.Duration;
import java.time.Instant;

import de.malkusch.ha.automation.model.shutters.Shutter.Api.State;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Shutter {

    public static interface Api {
        void setState(State state) throws ApiException, InterruptedException;

        State state() throws ApiException, InterruptedException;

        static record State(int percent) {

            public static final State OPEN = new State(0);
            public static final State CLOSED = new State(100);

            public State(int percent) {
                if (percent < 0 || percent > 100) {
                    throw new IllegalArgumentException("percent must be between 0 and 100");
                }
                this.percent = percent;
            }

            @Override
            public String toString() {
                return String.format("%d%%", percent);
            }
        }
    }

    public final ShutterId id;
    private final Api api;
    private final Duration delay;

    private State desired;

    public Shutter(ShutterId id, Api api, Duration delay) throws ApiException, InterruptedException {
        this.id = requireNonNull(id);
        this.api = requireNonNull(api);
        this.delay = requireNonNull(delay);
        desired = api.state();
    }

    public final void open() throws ApiException, InterruptedException {
        syncState();
        setState(OPEN);
    }

    public final void close() throws ApiException, InterruptedException {
        syncState();
        setState(CLOSED);
    }

    private void setState(State state) throws InterruptedException, ApiException {
        synchronized (_jvmLock) {
            desired = state;
            if (isLocked()) {
                log.info("Shutter {} is locked", this);
                return;
            }
        }
        if (api.state().equals(state)) {
            return;
        }

        log.info("Closing shutter {} to {}", this, state);
        api.setState(state);
        MILLISECONDS.sleep(delay.toMillis());
    }

    private static record Lock(State state, Instant until) {
    }

    private static final Lock UNLOCKED = new Lock(OPEN, Instant.MIN);
    private Lock lock = UNLOCKED;
    private final Object _jvmLock = new Object();

    public final void lock(State state, Duration lockDuration)
            throws ApiException, InterruptedException, LockedException {
        synchronized (_jvmLock) {
            syncState();
            if (isLocked()) {
                throw new LockedException(this + " is already locked");
            }
            log.info("Locking shutter {} to {}", this, state);
            desired = api.state();
            api.setState(state);
            lock = new Lock(state, now().plus(lockDuration));
        }
        MILLISECONDS.sleep(delay.toMillis());
    }

    private void syncState() throws ApiException, InterruptedException {
        synchronized (_jvmLock) {
            var state = api.state();
            if (isLocked() && !state.equals(lock.state)) {
                lock = UNLOCKED;
                desired = state;
            }
        }
    }

    public static class LockedException extends Exception {
        private static final long serialVersionUID = -1327223206692643421L;

        public LockedException(String message) {
            super(message);
        }
    }

    public final void unlock() throws ApiException, InterruptedException {
        synchronized (_jvmLock) {
            syncState();
            if (!isLocked()) {
                return;
            }
            log.info("Unlocking shutter {} to {}", this, desired);
            lock = UNLOCKED;
            setState(desired);
        }
    }

    boolean isLocked() {
        synchronized (_jvmLock) {
            return lock.until.isAfter(now());
        }
    }

    @Override
    public final String toString() {
        return id.name();
    }
}
