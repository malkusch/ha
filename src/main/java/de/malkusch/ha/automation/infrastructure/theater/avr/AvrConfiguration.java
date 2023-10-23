package de.malkusch.ha.automation.infrastructure.theater.avr;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.theater.avr.denon.DenonAvrFactory;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.automation.model.theater.Theater;
import de.malkusch.ha.shared.infrastructure.event.AsyncEventPublisher;
import de.malkusch.ha.shared.infrastructure.event.DebouncingEventPublisher;
import de.malkusch.ha.shared.infrastructure.event.EventPublisher;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class AvrConfiguration {

    private final Properties properties;

    @ConfigurationProperties("theater")
    @Component
    @Data
    public static class Properties {

        private RoomId room;
        private Avr avr;

        @Data
        public static class Avr {
            private String host;
            private Duration timeout;
            private Duration debouncingInterval;
        }

    }

    @Bean
    Theater theater() {
        return new Theater(properties.room);
    }

    @Bean
    AvrEventPublisher avrEventPublisher(EventPublisher publisher) {
        publisher = new AsyncEventPublisher("Denon-Event", publisher);
        publisher = new DebouncingEventPublisher(publisher, properties.avr.debouncingInterval);
        return new AvrEventPublisher(publisher);
    }

    @Bean
    ReconnectingAvr avr(AvrEventPublisher publisher) throws InterruptedException {
        var factory = new DenonAvrFactory(publisher, properties.avr.host, properties.avr.timeout);
        return new ReconnectingAvr(factory, publisher);
    }
}
