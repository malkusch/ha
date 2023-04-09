package de.malkusch.ha.automation.infrastructure.socket;

import java.io.IOException;
import java.util.Map;

import org.smarthomej.binding.tuya.internal.local.TuyaDevice;

import de.malkusch.ha.automation.infrastructure.socket.TuyaState.Power;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TuyaSocket implements Socket, AutoCloseable {

    final TuyaDevice tuyaDevice;
    final TuyaState state;

    @Override
    public void turnOn() {
        log.debug("Turn on");
        state.expire();
        tuyaDevice.set(Map.of(1, true));
    }

    @Override
    public void turnOff() {
        log.debug("Turn off");
        state.expire();
        tuyaDevice.set(Map.of(1, false));
    }

    @Override
    public boolean isOn() throws IOException {
        var power = state.state().power();
        return power == Power.ON;
    }

    @Override
    public void close() throws Exception {
        close(tuyaDevice);
        state.close();
    }
    
    static void close(TuyaDevice device) {
        device.dispose();
    }
}
