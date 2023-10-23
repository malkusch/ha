package de.malkusch.ha.shared.infrastructure.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface EventPublisher extends AutoCloseable {

    static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    void publish(Event event);

    default void publishSafely(Event event) {
        try {
            publish(event);

        } catch (Exception e) {
            log.error("Failed publishing {}", event.getClass().getName(), e);
        }
    }

    @Override
    default void close() throws Exception {
    }
}
