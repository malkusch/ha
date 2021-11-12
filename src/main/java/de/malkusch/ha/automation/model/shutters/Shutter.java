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
        setState(OPEN);
    }

    public final void close() throws ApiException, InterruptedException {
        setState(CLOSED);
    }

    private void setState(State state) throws InterruptedException, ApiException {
        synchronized (lock) {
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

    private static final Instant UNLOCKED = Instant.MIN;
    private Instant lockedUntil = UNLOCKED;
    private final Object lock = new Object();

    public void lock(State state, Duration lockDuration) throws ApiException, InterruptedException, LockedException {
        synchronized (lock) {
            if (isLocked()) {
                throw new LockedException(this + " is already locked");
            }
            log.info("Locking shutter {} to {}", this, state);
            desired = api.state();
            api.setState(state);
            lockedUntil = now().plus(lockDuration);
        }
        MILLISECONDS.sleep(delay.toMillis());
    }

    public static class LockedException extends Exception {
        private static final long serialVersionUID = -1327223206692643421L;

        public LockedException(String message) {
            super(message);
        }
    }

    public void unlock() throws ApiException, InterruptedException {
        synchronized (lock) {
            if (!isLocked()) {
                return;
            }
            log.info("Unlocking shutter {} to {}", this, desired);
            lockedUntil = UNLOCKED;
            setState(desired);
        }
    }

    private boolean isLocked() {
        synchronized (lock) {
            return lockedUntil.isAfter(now());
        }
    }

    @Override
    public final String toString() {
        return id.name();
    }
}
