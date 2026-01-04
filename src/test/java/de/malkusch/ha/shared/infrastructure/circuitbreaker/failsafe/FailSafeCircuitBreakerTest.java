package de.malkusch.ha.shared.infrastructure.circuitbreaker.failsafe;

import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreaker;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerConfiguration;
import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreakerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

public class FailSafeCircuitBreakerTest {

    @FunctionalInterface
    interface ThrowException {
        void run() throws IOException, InterruptedException;

        static ThrowException with(CircuitBreaker circuitBreaker, Throwable e) {
            ThrowException throwException = () -> {
                switch (e) {
                    case IOException io -> throw io;
                    case InterruptedException it -> throw it;
                    case RuntimeException r -> throw r;
                    default -> throw new IllegalStateException(e);
                }
            };
            return () -> circuitBreaker.<IOException, InterruptedException>run(throwException::run);
        }
    }

    @Test
    void shouldThrowBothCheckedExceptions() {
        var circuitBreaker = withFailureThreshold(10);

        assertThrowsExactly(IOException.class, ThrowException.with(circuitBreaker, new IOException())::run);
        assertThrowsExactly(InterruptedException.class, ThrowException.with(circuitBreaker, new InterruptedException())::run);
    }

    @Test
    void shouldThrowAnyRuntimeException() {
        var circuitBreaker = withFailureThreshold(10);
        class AnyRuntimeException extends RuntimeException {
        }

        assertThrowsExactly(AnyRuntimeException.class, ThrowException.with(circuitBreaker, new AnyRuntimeException())::run);
    }

    public static Throwable[] SHOULD_OPEN() {
        return new Throwable[]{new RuntimeException(), new IOException(), new Exception()};
    }

    @ParameterizedTest
    @MethodSource("SHOULD_OPEN")
    void shouldOpen(Throwable exception) {
        var circuitBreaker = withFailureThreshold(10);

        Executable execution = () -> circuitBreaker.call(() -> {
            throw exception;
        });

        for (int i = 0; i < 10; i++) {
            try {
                execution.execute();
            } catch (Throwable ignore) {
            }
        }

        assertThrows(CircuitBreakerException.class, execution);
    }

    CircuitBreaker withFailureThreshold(int failures) {
        return new FailSafeCircuitBreaker("any", new CircuitBreakerConfiguration(failures, 10, Duration.ofSeconds(20)), Throwable.class);
    }
}
