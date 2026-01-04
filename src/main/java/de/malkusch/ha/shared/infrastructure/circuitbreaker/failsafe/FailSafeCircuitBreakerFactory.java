package de.malkusch.ha.shared.infrastructure.circuitbreaker.failsafe;

import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreaker;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerConfiguration;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class FailSafeCircuitBreakerFactory implements CircuitBreakerFactory {

    private final CircuitBreakerConfiguration defaults;

    @Override
    public CircuitBreaker buildCircuitBreaker(String name) {
        return new FailSafeCircuitBreaker(name, defaults, Throwable.class);
    }
}
