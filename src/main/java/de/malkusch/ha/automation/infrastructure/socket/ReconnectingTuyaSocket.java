package de.malkusch.ha.automation.infrastructure.socket;

import static java.lang.Thread.currentThread;
import static java.time.Duration.between;
import static java.time.Instant.now;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import org.smarthomej.binding.tuya.internal.local.DeviceStatusListener;
import org.smarthomej.binding.tuya.internal.local.TuyaDevice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ReconnectingTuyaSocket implements Socket, AutoCloseable {

    private final Socket socket;
    private final TuyaDevice device;
    private final Duration timeout;

    public ReconnectingTuyaSocket(Socket socket, TuyaDevice device, Duration timeout, ReconnectListener listener) {
        this.socket = socket;
        this.device = device;
        this.timeout = timeout;

        listener.socket = this;
    }

    @RequiredArgsConstructor
    static class ReconnectListener implements DeviceStatusListener {

        private final DeviceStatusListener listener;
        private volatile ReconnectingTuyaSocket socket;

        @Override
        public void processDeviceStatus(Map<Integer, Object> deviceStatus) {
            listener.processDeviceStatus(deviceStatus);
        }

        @Override
        public void connectionStatus(boolean status) {
            listener.connectionStatus(status);
            if (socket != null) {
                socket.updateConnected(status);
            }
        }
    }

    private volatile boolean connected = true;
    private final Object updateLock = new Object();

    void updateConnected(boolean connected) {
        synchronized (updateLock) {
            this.connected = connected;
            updateLock.notifyAll();
        }
    }

    private boolean awaitConnected() {
        var start = now();
        var waitUntil = start.plus(timeout);
        synchronized (updateLock) {
            while (!connected) {
                if (Thread.interrupted()) {
                    currentThread().interrupt();
                    return connected;
                }
                if (now().isAfter(waitUntil)) {
                    return connected;
                }
                try {
                    updateLock.wait(100);

                } catch (InterruptedException e) {
                    currentThread().interrupt();
                    return connected;
                }
            }
        }
        log.debug("Connected after {} ms", between(start, now()).toMillis());
        return connected;
    }

    @Override
    public void turnOn() {
        reconnected(socket::turnOn);
    }

    @Override
    public void turnOff() {
        reconnected(socket::turnOff);
    }

    private volatile boolean lastOn = false;

    @Override
    public boolean isOn() throws IOException {
        lastOn = reconnected(socket::isOn, lastOn);
        return lastOn;
    }

    @Override
    public boolean isOnline() {
        return connected && socket.isOnline();
    }

    private static interface Query<T, E extends Exception> {

        T query() throws E;

    }

    private <T, E extends Exception> T reconnected(Query<T, E> query, T fallback) throws E {
        if (!connected) {
            log.warn("Socket is not connected, trying to reconnect");
            device.dispose();
            device.connect();
            if (!awaitConnected()) {
                device.dispose();
                log.warn("Reconnect failed, returning a silent fallback");
                return fallback;
            }
            log.info("Reconnected successfully");
        }
        return query.query();
    }

    private void reconnected(Runnable operation) {
        Query<Void, ? extends RuntimeException> query = () -> {
            operation.run();
            return null;
        };
        reconnected(query, null);
    }

    @Override
    public void close() throws Exception {
        socket.close();
        TuyaSocket.close(device);
    }
}
