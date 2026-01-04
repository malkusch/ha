package de.malkusch.ha.shared.infrastructure.circuitbreaker.failsafe;

import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerConfiguration;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringConfiguration {

    @Bean
    CircuitBreakerFactory circuitBreakerFactory(CircuitBreakerConfiguration configuration) {
        return new FailSafeCircuitBreakerFactory(configuration);
    }

}
