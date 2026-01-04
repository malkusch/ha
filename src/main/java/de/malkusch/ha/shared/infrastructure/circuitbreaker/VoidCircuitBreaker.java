package de.malkusch.ha.shared.infrastructure.circuitbreaker;

import de.malkusch.ha.shared.infrastructure.circuitbreaker.CircuitBreaker.Execution;

public interface VoidCircuitBreaker {

    @FunctionalInterface
    interface VoidExecution<E1 extends Throwable, E2 extends Throwable> extends Execution<Void, E1, E2> {
        void run() throws E1, E2;

        @Override
        default Void call() throws E1, E2 {
            run();
            return null;
        }
    }

    <E1 extends Throwable, E2 extends Throwable> void run(VoidExecution<E1, E2> execution) throws E1, E2;
}
