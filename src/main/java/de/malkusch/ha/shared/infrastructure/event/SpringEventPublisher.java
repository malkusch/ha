package de.malkusch.ha.shared.infrastructure.event;

import org.springframework.context.ApplicationEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
final class SpringEventPublisher implements EventPublisher {

    private final ApplicationEventPublisher publisher;

    @Override
    public void publish(Event event) {
        log.debug("publish {}", event);
        publisher.publishEvent(event);
    }

    @Override
    public String toString() {
        return "SpringEventPublisher";
    }
}
