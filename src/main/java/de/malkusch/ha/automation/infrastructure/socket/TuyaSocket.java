package de.malkusch.ha.automation.infrastructure.socket;

import java.io.IOException;

import de.malkusch.tuya.TuyaApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TuyaSocket implements Socket, AutoCloseable {

    private final TuyaApi tuya;

    @Override
    public void turnOn() throws IOException {
        log.debug("Turn on");
        tuya.turnOn();
    }

    @Override
    public void turnOff() throws IOException {
        log.debug("Turn off");
        tuya.turnOff();
    }

    @Override
    public boolean isOn() throws IOException {
        return tuya.isOn();
    }

    @Override
    public boolean isOnline() {
        return tuya.isOnline();
    }

    @Override
    public void close() throws Exception {
        tuya.close();
    }
    
    @Override
    public String toString() {
        return "TuyaSocket";
    }
}
