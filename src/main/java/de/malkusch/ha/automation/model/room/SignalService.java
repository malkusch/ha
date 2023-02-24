package de.malkusch.ha.automation.model.room;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.light.Color;
import de.malkusch.ha.automation.model.light.LightRepository;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignalService {

    private final LightRepository lights;

    private static final Color OPEN_WINDOWS = new Color(255, 0, 0);

    public void signalOpenWindows(Room room) throws ApiException {
        log.debug("signalOpenWindows() in {}", room);
        signal(room, OPEN_WINDOWS);
    }

    private static final Color OLD_AIR = new Color(255, 100, 100);

    public void signalOldAir(Room room) throws ApiException {
        log.debug("signalOldAir() in {}", room);
        signal(room, OLD_AIR);
    }

    private static final Color CLOSE_WINDOWS = new Color(0, 255, 0);

    public void signalCloseWindows(Room room) throws ApiException {
        log.debug("signalCloseWindows() in {}", room);
        signal(room, CLOSE_WINDOWS);
    }

    private void signal(Room room, Color color) throws ApiException {
        var light = lights.find(room.signal);
        light.changeColor(color);
        light.turnOn();
    }
}
