package de.malkusch.ha.shared.infrastructure.event;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class DeferredEventPublisher implements EventPublisher {

    private final List<Event> queue = new ArrayList<>();
    private final Object lock = new Object();
    private volatile EventPublisher publisher;

    @Override
    public void publish(Event event) {
        synchronized (lock) {
            if (publisher != null) {
                log.debug("Forward event {}", event);
                publisher.publish(event);

            } else {
                defer(event);
            }
        }
    }

    @Override
    public void publishSafely(Event event) {
        synchronized (lock) {
            if (publisher != null) {
                log.debug("Forward event {}", event);
                publisher.publishSafely(event);

            } else {
                defer(event);
            }
        }
    }

    private void defer(Event event) {
        synchronized (lock) {
            log.debug("Defer event {}", event);
            queue.add(event);
        }
    }

    void forward(EventPublisher publisher) {
        synchronized (lock) {
            if (this.publisher != null) {
                throw new IllegalStateException("Events already forwarded");
            }
            this.publisher = publisher;
            log.info("Publishing {} deferred events", queue.size());
            queue.forEach(publisher::publishSafely);
            queue.clear();
        }
    }

    @Override
    public String toString() {
        return "DeferredEvents";
    }
}
