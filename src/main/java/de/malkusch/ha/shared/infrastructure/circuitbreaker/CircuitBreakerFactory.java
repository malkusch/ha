package de.malkusch.ha.shared.infrastructure.circuitbreaker;

public interface CircuitBreakerFactory {

    CircuitBreaker buildCircuitBreaker(String name);

    CircuitBreaker buildCircuitBreaker(String name, CircuitBreakerConfiguration configuration);

    default VoidCircuitBreaker buildSilentCircuitBreaker(String name) {
        return new SilentCircuitBreaker(buildCircuitBreaker(name));
    }

    default VoidCircuitBreaker buildSilentCircuitBreaker(String name, CircuitBreakerConfiguration configuration) {
        return new SilentCircuitBreaker(buildCircuitBreaker(name, configuration));
    }
}
