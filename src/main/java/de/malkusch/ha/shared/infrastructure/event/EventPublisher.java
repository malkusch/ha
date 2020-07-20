package de.malkusch.ha.shared.infrastructure.event;

import org.springframework.context.ApplicationEventPublisher;

public final class EventPublisher {

    static volatile ApplicationEventPublisher publisher;

    public static void publish(Event event) {
        publisher.publishEvent(event);
    }
}
