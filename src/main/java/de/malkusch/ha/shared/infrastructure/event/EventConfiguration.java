package de.malkusch.ha.shared.infrastructure.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventConfiguration {

    EventConfiguration(ApplicationEventPublisher publisher) {
        EventPublisher.publisher = publisher;
    }
}
