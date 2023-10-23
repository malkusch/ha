package de.malkusch.ha.shared.infrastructure.event;

import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class EventConfiguration {

    @Bean
    EventPublisher eventPublisher() {
        return StaticEventPublisher.PUBLISHER;
    }

    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener(ApplicationStartedEvent.class)
    void setup() throws Exception {
        var springPublisher = new SpringEventPublisher(applicationEventPublisher);
        StaticEventPublisher.PUBLISHER.setup(springPublisher);
    }
}
