package de.malkusch.ha.shared.infrastructure.circuitbreaker;


public interface CircuitBreaker extends VoidCircuitBreaker {

    @FunctionalInterface
    interface Execution<R, E1 extends Throwable, E2 extends Throwable> {
        R call() throws E1, E2;
    }

    <R, E1 extends Throwable, E2 extends Throwable> R call(Execution<R, E1, E2> execution) throws E1, E2;

    @Override
    default <E1 extends Throwable, E2 extends Throwable> void run(VoidExecution<E1, E2> execution) throws E1, E2 {
        call(execution);
    }
}
