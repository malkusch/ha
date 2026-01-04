package de.malkusch.ha.shared.infrastructure.circuitbreaker.failsafe;

import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreaker;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerConfiguration;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerException;
import dev.failsafe.Failsafe;
import dev.failsafe.FailsafeException;
import dev.failsafe.FailsafeExecutor;
import lombok.extern.slf4j.Slf4j;

import static dev.failsafe.CircuitBreaker.builder;

@Slf4j
final class FailSafeCircuitBreaker implements CircuitBreaker {

    private final String name;
    private final FailsafeExecutor<? super Object> failsafe;

    @SafeVarargs
    FailSafeCircuitBreaker(String name, CircuitBreakerConfiguration configuration,
                           Class<? extends Throwable>... exceptions) {

        this.name = name;
        var circuitBreaker = builder() //
                .handle(exceptions) //
                .withFailureThreshold(configuration.failureThreshold()) //
                .withDelay(configuration.delay()) //
                .withSuccessThreshold(configuration.successThreshold()) //
                .onClose(it -> onClose()) //
                .onOpen(it -> onOpen()) //
                .onHalfOpen(it -> onHalfOpen()) //
                .build();
        failsafe = Failsafe.with(circuitBreaker);
    }

    @Override
    public <R, E1 extends Throwable, E2 extends Throwable> R call(Execution<R, E1, E2> execution) throws E1, E2 {
        try {
            return failsafe.get(execution::call);

        } catch (dev.failsafe.CircuitBreakerOpenException e) {
            throw new CircuitBreakerException("Open circuit breaker " + this, e);

        } catch (FailsafeException e) {
            throw (E1) e.getCause();
        }
    }

    private void onClose() {
        log.info("Closed circuit breaker {}", this);
    }

    private void onHalfOpen() {
        log.info("Half opened circuit breaker {}", this);
    }

    private void onOpen() {
        log.warn("Opened circuit breaker {}", this);
    }

    @Override
    public String toString() {
        return name;
    }
}