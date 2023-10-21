package de.malkusch.ha.automation.infrastructure.theater.avr.denon;

import static de.malkusch.ha.shared.infrastructure.scheduler.Schedulers.singleThreadScheduler;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import de.malkusch.ha.automation.infrastructure.theater.avr.Avr;
import de.malkusch.ha.shared.infrastructure.scheduler.Schedulers;
import io.theves.denon4j.AccessibleDenonReceiver;
import io.theves.denon4j.controls.Control;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class DenonAvrFactory implements Avr.Factory, AutoCloseable {

    private final ScheduledExecutorService check_connection_thread = singleThreadScheduler("denon-connection-check");
    private final EventPublisher publisher = new EventPublisher();

    final class DenonAvr implements Avr, AutoCloseable {

        final AccessibleDenonReceiver denon;

        DenonAvr(AccessibleDenonReceiver denon) throws Exception {
            this.denon = denon;

            registerEventHandler(new PowerEvent(denon.protocol(), publisher));
        }

        private void registerEventHandler(Control handler) {
            handler.init();
            denon.eventDispatcher().addControl(handler);
        }

        @Override
        public boolean isConnected() {
            if (!denon.isConnected() || !canConnect()) {
                return false;
            }

            try {
                check_connection_thread.submit(() -> denon.mainZone().state()) //
                        .get(timeout.toMillis(), MILLISECONDS);
                return true;

            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public void close() throws Exception {
            log.info("Disconnecting {}", this);
            denon.close();
        }

        @Override
        public String toString() {
            return String.format("AVR(%s)", host);
        }
    }

    private final String host;
    private final Duration timeout;

    @Override
    public Avr connect() throws Exception {
        if (!canConnect()) {
            throw new IOException("Can't connect to " + host);
        }

        var daemonThread = singleThreadScheduler("denon-factory");
        try {
            // That's necessary to start Denon's EventReader thread as daemon
            var denon = daemonThread.submit(() -> AccessibleDenonReceiver.build(host, null)) //
                    .get(timeout.toMillis(), MILLISECONDS);
            try {
                var avr = new DenonAvr(denon);
                daemonThread.submit(() -> denon.connect((int) timeout.toMillis())) //
                        .get(timeout.toMillis(), MILLISECONDS);
                return avr;

            } catch (Exception e) {
                try (denon) {
                    throw e;
                }
            }
        } finally {
            Schedulers.close(daemonThread);
        }
    }

    @Override
    public boolean canConnect() {
        try {
            var address = InetAddress.getByName(host);
            return address.isReachable((int) timeout.toMillis());

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("AVR(%s)", host);
    }

    @Override
    public void close() throws Exception {
        Schedulers.close(check_connection_thread);
        publisher.close();
    }
}
