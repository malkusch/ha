package de.malkusch.ha.shared.infrastructure.circuitbreaker;

public interface CircuitBreakerFactory {

    CircuitBreaker buildCircuitBreaker(String name);

    default VoidCircuitBreaker buildSilentCircuitBreaker(String name) {
        return new SilentCircuitBreaker(buildCircuitBreaker(name));
    }
}
