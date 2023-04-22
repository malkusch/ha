package de.malkusch.ha.automation.infrastructure.socket;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.springframework.scheduling.annotation.Scheduled;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OfflineSocket implements Socket {

    private final Callable<Socket> factory;
    private volatile Socket socket;

    public OfflineSocket(Callable<Socket> factory) {
        this.factory = factory;
        try {
            socket = factory.call();

        } catch (Exception e) {
            log.warn("Falling back to NullSocket: {}", e.getMessage());
            socket = NullSocket.NULL_SOCKET;
        }
    }

    @Scheduled(cron = "${electricity.scooter.wallbox.tuya-socket.discover-cron}")
    void discover() {
        if (socket != NullSocket.NULL_SOCKET) {
            return;
        }
        try {
            log.info("Try to discover an online socket");
            socket = factory.call();
            log.info("Successfully discovered online socket");

        } catch (Exception e) {
            log.warn("Discovery failed, keeping NullSocket: {}", e.getMessage());
        }
    }

    @Slf4j
    private static class NullSocket implements Socket {

        private static final Socket NULL_SOCKET = new NullSocket();

        @Override
        public void close() throws Exception {
        }

        private volatile boolean power = false;

        @Override
        public void turnOn() {
            log.info("turnOn()");
            power = true;
        }

        @Override
        public void turnOff() {
            log.info("turnOff()");
            power = false;
        }

        @Override
        public boolean isOn() throws IOException {
            log.info("isOn()");
            return power;
        }

        @Override
        public boolean isOnline() {
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    @Override
    public void turnOn() throws IOException {
        socket.turnOn();
    }

    @Override
    public void turnOff() throws IOException {
        socket.turnOff();
    }

    @Override
    public boolean isOn() throws IOException {
        return socket.isOn();
    }

    @Override
    public boolean isOnline() {
        return socket.isOnline();
    }
}
