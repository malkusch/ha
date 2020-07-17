package de.malkusch.ha.automation.infrastructure;

import java.time.Duration;
import java.time.Instant;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Debouncer {

    public static class DebounceException extends Exception {
        private static final long serialVersionUID = -8894286463794669140L;

        public final Instant retryAfter;

        public DebounceException(Instant retryAfter, String message) {
            super(message);
            this.retryAfter = retryAfter;
        }
    }

    private final Duration window;
    private volatile Instant debounceUntil = Instant.now();

    public void debounce() throws DebounceException {
        if (Instant.now().isBefore(debounceUntil)) {
            throw new DebounceException(debounceUntil, "Retry after " + debounceUntil);
        }
        debounceUntil = Instant.now().plus(window);
    }
}
