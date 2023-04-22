package de.malkusch.ha.automation.infrastructure.scooter;

import java.io.IOException;

import de.malkusch.ha.automation.infrastructure.socket.Socket;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.Api;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TuyaWallboxApi implements Api, AutoCloseable {

    private final Socket socket;

    @Override
    public void start() throws IOException {
        socket.turnOn();
    }

    @Override
    public boolean isCharging() throws IOException {
        return socket.isOn();
    }

    @Override
    public void stop() throws IOException {
        socket.turnOff();
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    @Override
    public boolean isOnline() {
        return socket.isOnline();
    }
}
