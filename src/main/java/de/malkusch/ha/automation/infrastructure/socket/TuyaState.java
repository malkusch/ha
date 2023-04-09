package de.malkusch.ha.automation.infrastructure.socket;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Thread.currentThread;
import static java.time.Duration.between;
import static java.time.Instant.now;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.smarthomej.binding.tuya.internal.local.DeviceStatusListener;
import org.smarthomej.binding.tuya.internal.local.TuyaDevice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class TuyaState implements AutoCloseable {

    private final TuyaDevice tuyaDevice;
    private final Duration timeout;
    private final Duration expiration;

    static enum Power {
        ON, OFF;
    }

    static record State(Power power, Instant time) {
    }

    private volatile State state;
    private volatile Instant expireAt;

    TuyaState(TuyaDevice tuyaDevice, StateUpdater updater, Duration timeout, Duration expiration) throws IOException {
        this.tuyaDevice = tuyaDevice;

        this.timeout = timeout;
        log.info("State timeout after {}", timeout);

        this.expiration = expiration;
        log.info("Expire state after {}", expiration);

        this.state = new State(Power.OFF, Instant.MIN);
        expireAt = Instant.MIN;

        updater.enable(this);
        waitForState();
        expireAt = state.time.plus(expiration);
    }

    void expire() {
        log.debug("Expire state");
        expireAt = Instant.MIN;
    }

    private boolean isExpired() {
        var now = now();
        return now.isAfter(expireAt) || now.equals(expireAt);
    }

    public State state() throws IOException {
        synchronized (statusLock) {
            if (isExpired()) {
                log.debug("Refreshing expired state");
                tuyaDevice.refreshStatus();
                tuyaDevice.requestStatus();
                waitForState();
            }
            return state;
        }
    }

    private void waitForState() throws IOException {
        var start = now();
        var waitUntil = start.plus(timeout);
        synchronized (statusLock) {
            while (isExpired()) {
                if (now().isAfter(waitUntil)) {
                    throw new IOException(
                            "Waiting for state timed out after " + between(start, now()).toMillis() + " ms");
                }
                if (Thread.interrupted()) {
                    currentThread().interrupt();
                    throw new IOException("Waiting for state was interrupted");
                }

                try {
                    var waitMillis = waitMillis(waitUntil);
                    statusLock.wait(waitMillis);

                } catch (InterruptedException e) {
                    currentThread().interrupt();
                    throw new IOException("Waiting for state was interrupted", e);
                }
            }
        }
        log.debug("Received state after {} ms", between(start, now()).toMillis());
    }

    private static final long MAX_WAIT_MILLIS = 200;
    private static final long MIN_WAIT_MILLIS = 10;

    private static long waitMillis(Instant waitUntil) {
        var millisUntil = max(MIN_WAIT_MILLIS, between(now(), waitUntil).toMillis());
        return min(MAX_WAIT_MILLIS, millisUntil);
    }

    private final Object statusLock = new Object();

    @Slf4j
    static class StateUpdater implements DeviceStatusListener {

        private volatile TuyaState state;
        private final CountDownLatch latch = new CountDownLatch(1);

        @Override
        public void processDeviceStatus(Map<Integer, Object> deviceStatus) {
            log.debug("Received status {}", deviceStatus);
            waitUntilEnabled();
            state.updateState(deviceStatus);
        }

        private void waitUntilEnabled() {
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Initialization was interrupted", e);
            }
        }

        private void enable(TuyaState state) {
            this.state = state;
            latch.countDown();
        }

        @Override
        public void connectionStatus(boolean status) {
        }
    }

    void updateState(Map<Integer, Object> deviceStatus) {
        log.debug("Received state update: {}", deviceStatus);
        if (deviceStatus.get(1) instanceof Boolean on) {
            synchronized (statusLock) {
                state = new State(on ? Power.ON : Power.OFF, now());
                expireAt = state.time.plus(expiration);
                log.debug("Updated state: {}", state);
                statusLock.notifyAll();
            }
        }
    }

    @Override
    public void close() throws Exception {
        tuyaDevice.dispose();
    }
}
