package de.malkusch.ha.automation.model.room;

import de.malkusch.ha.automation.model.light.Color;
import de.malkusch.ha.automation.model.light.Light;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class Room {

    public final RoomId id;
    public final Light signal;

    private static final Color OPEN_WINDOWS = new Color(255, 0, 0);

    public void signalOpenWindows() throws ApiException {
        log.debug("signalOpenWindows() in {}", id);
        signal.changeColor(OPEN_WINDOWS);
        signal.turnOn();
    }

    private static final Color OLD_AIR = new Color(255, 100, 100);

    public void signalOldAir() throws ApiException {
        log.debug("signalOldAir() in {}", id);
        signal.changeColor(OLD_AIR);
        signal.turnOn();
    }

    private static final Color CLOSE_WINDOWS = new Color(0, 255, 0);

    public void signalCloseWindows() throws ApiException {
        log.debug("signalCloseWindows() in {}", id);
        signal.changeColor(CLOSE_WINDOWS);
        signal.turnOn();
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
