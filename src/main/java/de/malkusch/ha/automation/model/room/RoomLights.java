package de.malkusch.ha.automation.model.room;

import java.util.Collection;

import de.malkusch.ha.automation.model.light.Light;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RoomLights {

    private final Collection<Light> lights;

    public void turnOn() throws ApiException {
        for (var light : lights) {
            light.turnOn();
        }
    }

    public void turnOff() throws ApiException {
        for (var light : lights) {
            light.turnOff();
        }
    }
}
