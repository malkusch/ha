package de.malkusch.ha.shared.infrastructure.event;

import org.springframework.context.ApplicationEventPublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EventPublisher {

    static volatile ApplicationEventPublisher publisher;

    public static void publish(Event event) {
        publisher.publishEvent(event);
    }

    public static void publishSafely(Event event) {
        try {
            publish(event);

        } catch (Exception e) {
            log.error("Failed publishing {}", event.getClass().getName(), e);
        }
    }
}
