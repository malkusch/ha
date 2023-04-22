package de.malkusch.ha.shared.infrastructure;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatDuration;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;
import static java.time.Instant.now;

import java.time.Duration;
import java.time.Instant;

public final class CoolDown {

    private final Duration coolDown;
    private volatile Instant coolDownUntil;

    public CoolDown(Duration coolDown) {
        this.coolDown = coolDown;
        coolDownUntil = now().plus(coolDown);
    }

    @FunctionalInterface
    public static interface CoolDownOperation<E1 extends Throwable, E2 extends Throwable> {
        void execute() throws E1, E2;
    }

    public <E1 extends Throwable, E2 extends Throwable> void withSilentCoolDown(CoolDownOperation<E1, E2> operation)
            throws E1, E2 {

        execute(operation);
    }

    public <E1 extends Throwable, E2 extends Throwable> void withCoolDown(CoolDownOperation<E1, E2> operation)
            throws E1, E2, CoolDownException {

        if (!execute(operation)) {
            throw new CoolDownException(coolDownUntil, "Retry after " + formatTime(coolDownUntil));
        }
    }

    public static class CoolDownException extends Exception {
        private static final long serialVersionUID = -8894286463794669140L;

        public final Instant retryAfter;

        public CoolDownException(Instant retryAfter, String message) {
            super(message);
            this.retryAfter = retryAfter;
        }
    }

    private final Object _lock = new Object();

    private <E1 extends Throwable, E2 extends Throwable> boolean execute(CoolDownOperation<E1, E2> operation)
            throws E1, E2 {

        synchronized (_lock) {
            if (now().isBefore(coolDownUntil)) {
                return false;
            }
            coolDownUntil = now().plus(coolDown);
        }

        operation.execute();
        return true;
    }

    @Override
    public String toString() {
        return formatDuration(coolDown);
    }
}
