package de.malkusch.ha.shared.infrastructure.circuitbreaker;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class SilentCircuitBreaker implements VoidCircuitBreaker {

    private final VoidCircuitBreaker circuitBreaker;

    @Override
    public <E1 extends Throwable, E2 extends Throwable> void run(VoidExecution<E1, E2> execution) throws E1, E2 {
        try {
            circuitBreaker.run(execution);

        } catch (CircuitBreakerException e) {
            log.warn("Circuit breaker {} is open", circuitBreaker);
        }
    }

    @Override
    public String toString() {
        return circuitBreaker.toString();
    }
}
