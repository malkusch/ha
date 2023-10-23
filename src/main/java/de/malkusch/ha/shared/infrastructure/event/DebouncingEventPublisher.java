package de.malkusch.ha.shared.infrastructure.event;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatDuration;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatExactTime;
import static de.malkusch.ha.shared.infrastructure.event.Event.NullEvent.NULL_EVENT;

import java.time.Duration;
import java.time.Instant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DebouncingEventPublisher implements EventPublisher {

    private final EventPublisher publisher;
    private final Duration debouncingInverval;

    public DebouncingEventPublisher(EventPublisher publisher, Duration debouncingInverval) {
        this.publisher = publisher;
        this.debouncingInverval = debouncingInverval;

        log.info("Debouncing {} for {}", publisher, formatDuration(debouncingInverval));
    }

    @Override
    public void publish(Event event) {
        if (isDebounced(event)) {
            return;
        }
        publisher.publish(event);
    }

    @Override
    public void publishSafely(Event event) {
        if (isDebounced(event)) {
            return;
        }
        publisher.publishSafely(event);
    }

    private volatile Instant expiration = Instant.now();
    private volatile Event last = NULL_EVENT;
    private final Object lock = new Object();

    private boolean isDebounced(Event event) {
        synchronized (lock) {
            var now = Instant.now();

            if (event.equals(last) && now.isBefore(expiration)) {
                log.debug("Debouncing event {} until {}", event, formatExactTime(expiration));
                return true;
            }

            last = event;
            expiration = now.plus(debouncingInverval);
            log.debug("Registered event {} until {}", event, formatExactTime(expiration));
            return false;
        }
    }

    @Override
    public String toString() {
        return publisher.toString();
    }

    @Override
    public void close() throws Exception {
        publisher.close();
    }
}
