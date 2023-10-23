package de.malkusch.ha.automation.infrastructure.theater.avr.denon;

import static de.malkusch.ha.shared.infrastructure.scheduler.Schedulers.singleThreadScheduler;
import static java.lang.Thread.interrupted;
import static java.util.Optional.ofNullable;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;

import de.malkusch.ha.automation.infrastructure.theater.avr.Avr;
import de.malkusch.ha.automation.infrastructure.theater.avr.AvrEventPublisher;
import de.malkusch.ha.shared.infrastructure.scheduler.Schedulers;
import de.malkusch.ha.shared.model.ApiException;
import io.theves.denon4j.AccessibleDenonReceiver;
import io.theves.denon4j.controls.Control;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public final class DenonAvrFactory implements Avr.Factory, AutoCloseable {

    private final ScheduledExecutorService apiThread = singleThreadScheduler("Denon-api");

    private final AvrEventPublisher publisher;
    private final String host;
    private final Duration timeout;

    final class DenonAvr implements Avr, AutoCloseable {

        private final AccessibleDenonReceiver denon;
        private final PowerEventHandler eventHandler;

        DenonAvr(AccessibleDenonReceiver denon) {
            this.denon = denon;
            eventHandler = new PowerEventHandler(denon.protocol(), publisher);

            registerEventHandler(eventHandler);
        }

        private void registerEventHandler(Control handler) {
            handler.init();
            denon.eventDispatcher().addControl(handler);
        }

        @Override
        public boolean isTurnedOn() throws ApiException, InterruptedException {
            var power = withTimeout(apiThread, denon.power()::state);
            return switch (power) {
            case ON -> true;
            case STANDBY, OFF -> false;
            };
        }

        @Override
        public boolean isConnected() throws InterruptedException {
            if (!denon.isConnected() || !canConnect()) {
                return false;
            }

            try {
                withTimeout(apiThread, denon.mainZone()::state);
                return true;

            } catch (ApiException e) {
                return false;
            }
        }

        @Override
        public void close() throws Exception {
            log.info("Disconnecting {}", this);
            try (denon) {
                denon.eventDispatcher().removeControl(eventHandler);
                eventHandler.dispose();
            }
        }

        @Override
        public String toString() {
            return String.format("AVR(%s)", host);
        }
    }

    @Override
    public Avr connect() throws ApiException, InterruptedException {
        if (!canConnect()) {
            throw new ApiException("Can't connect to " + host);
        }

        var daemonThread = singleThreadScheduler("denon-factory");
        try {
            // That's necessary to start Denon's EventReader thread as daemon
            var denon = withTimeout(daemonThread, () -> AccessibleDenonReceiver.build(host, null));

            try {
                var avr = new DenonAvr(denon);
                withTimeout(daemonThread, () -> denon.connect((int) timeout.toMillis()));
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

    private void withTimeout(ScheduledExecutorService executor, Runnable call)
            throws ApiException, InterruptedException {

        withTimeout(executor, () -> {
            call.run();
            return null;
        });
    }

    private <T> T withTimeout(ScheduledExecutorService executor, Callable<T> call)
            throws ApiException, InterruptedException {

        try {
            return executor.submit(call).get(timeout.toMillis(), MILLISECONDS);

        } catch (TimeoutException e) {
            throw new ApiException(e);

        } catch (ExecutionException e) {
            throw new ApiException(ofNullable(e.getCause()).orElse(e));
        }
    }

    @Override
    public boolean canConnect() throws InterruptedException {
        if (interrupted()) {
            throw new InterruptedException();
        }
        try {
            var address = InetAddress.getByName(host);
            return address.isReachable((int) timeout.toMillis());

        } catch (IOException e) {
            if (interrupted()) {
                throw new InterruptedException();
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return String.format("AVR(%s)", host);
    }

    @Override
    public void close() throws Exception {
        Schedulers.close(apiThread);
    }
}
