package de.malkusch.ha.automation.infrastructure.theater.avr;

import static de.malkusch.ha.automation.infrastructure.theater.avr.Avr.UnconnectedAvr.UNCONNECTED;

import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class ReconnectingAvr implements AutoCloseable {

    private volatile Avr avr;
    private final Avr.Factory factory;

    public ReconnectingAvr(Avr.Factory factory) {
        this.factory = factory;

        try {
            this.avr = factory.connect();

        } catch (Exception e) {
            avr = UNCONNECTED;
            log.warn("Can't connect to {}", factory);
        }
    }

    private final Object lock = new Object();

    @Scheduled(cron = "${theater.avr.reconnect-interval}")
    void checkConnection() throws Exception {
        synchronized (lock) {
            if (avr.isConnected()) {
                return;
            }

            if (avr != UNCONNECTED) {
                log.warn("Disconnected {}", avr);
                try (var old = avr) {
                    avr = UNCONNECTED;
                }
            }

            if (!factory.canConnect()) {
                log.warn("Unreachable {}", factory);
                return;
            }

            log.info("Reconnecting {}", factory);
            avr = factory.connect();
            log.info("Reconnected {}", avr);
        }
    }

    @Override
    public String toString() {
        return avr.toString();
    }

    @Override
    public void close() throws Exception {
        try (var close_avr = avr; factory) {
        }
    }
}
