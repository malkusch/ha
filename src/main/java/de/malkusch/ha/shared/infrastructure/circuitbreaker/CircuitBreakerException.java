package de.malkusch.ha.shared.infrastructure.circuitbreaker;

public class CircuitBreakerException extends RuntimeException {
    public CircuitBreakerException(String message, Throwable cause) {
        super(message, cause);
    }
}
