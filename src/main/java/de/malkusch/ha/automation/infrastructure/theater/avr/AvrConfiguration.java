package de.malkusch.ha.automation.infrastructure.theater.avr;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.theater.avr.denon.DenonAvrFactory;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.automation.model.theater.Theater;
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
        }

    }

    @Bean
    Theater theater() {
        return new Theater(properties.room);
    }

    @Bean
    ReconnectingAvr avr() {
        var factory = new DenonAvrFactory(properties.avr.host, properties.avr.timeout);
        return new ReconnectingAvr(factory);
    }
}
