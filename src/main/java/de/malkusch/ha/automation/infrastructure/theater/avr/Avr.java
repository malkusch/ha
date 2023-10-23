package de.malkusch.ha.automation.infrastructure.theater.avr;

import de.malkusch.ha.shared.model.ApiException;

public interface Avr extends AutoCloseable {

    public static interface Factory extends AutoCloseable {

        Avr connect() throws ApiException, InterruptedException;

        boolean canConnect() throws InterruptedException;

        @Override
        default void close() throws Exception {
        }
    }

    static enum UnconnectedAvr implements Avr {

        UNCONNECTED;

        @Override
        public boolean isTurnedOn() {
            return false;
        }

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public String toString() {
            return "AVR(UNCONNECTED)";
        }
    }

    boolean isTurnedOn() throws ApiException, InterruptedException;

    boolean isConnected() throws InterruptedException;

    @Override
    default void close() throws Exception {
    }
}
