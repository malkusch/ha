package de.malkusch.ha.automation.infrastructure.light;

import de.malkusch.broadlinkBulb.BroadlinkBulb;
import de.malkusch.ha.automation.model.light.Color;
import de.malkusch.ha.automation.model.light.Light.Api;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class BroadlinkLightApi implements Api {

    private final BroadlinkBulb bulb;

    @Override
    public void turnOn() throws ApiException {
        try {
            bulb.turnOn();
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    @Override
    public void turnOff() throws ApiException {
        try {
            bulb.turnOff();
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    @Override
    public void changeColor(Color color) throws ApiException {
        try {
            bulb.changeColor(map(color));
        } catch (Exception e) {
            throw new ApiException(e);
        }
    }

    private static de.malkusch.broadlinkBulb.Color map(Color color) {
        return new de.malkusch.broadlinkBulb.Color(color.red().value(), color.green().value(), color.blue().value());
    }
}
