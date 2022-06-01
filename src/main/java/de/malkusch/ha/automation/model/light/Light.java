package de.malkusch.ha.automation.model.light;

import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Light {

    public final LightId id;
    private final Api api;

    public static interface Api {

        void turnOn() throws ApiException;

        void turnOff() throws ApiException;

        void changeColor(Color color) throws ApiException;;

    }

    public void turnOn() throws ApiException {
        api.turnOn();
    }

    public void turnOff() throws ApiException {
        api.turnOff();
    }

    public void changeColor(Color color) throws ApiException {
        api.changeColor(color);
    }
}
