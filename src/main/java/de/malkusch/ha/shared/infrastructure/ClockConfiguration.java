package de.malkusch.ha.shared.infrastructure;

import static java.time.Clock.systemDefaultZone;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ClockConfiguration {

    @Bean
    Clock clock() {
        return systemDefaultZone();
    }

}
