package de.malkusch.ha.automation.infrastructure.theater.avr;

public interface Avr extends AutoCloseable {

    public static interface Factory extends AutoCloseable {

        Avr connect() throws Exception;

        boolean canConnect();

        @Override
        default void close() throws Exception {
        }
    }

    static enum UnconnectedAvr implements Avr {

        UNCONNECTED;

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public String toString() {
            return "AVR(UNCONNECTED)";
        }
    }

    boolean isConnected();

    @Override
    default void close() throws Exception {
    }
}
