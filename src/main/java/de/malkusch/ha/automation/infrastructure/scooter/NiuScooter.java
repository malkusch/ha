package de.malkusch.ha.automation.infrastructure.scooter;

import java.io.IOException;

import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.scooter.Scooter;
import de.malkusch.niu.Niu;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class NiuScooter implements Scooter {

    private final String serialNumber;
    private final Niu api;

    @Override
    public Capacity charge() throws IOException {
        var charge = api.batteryInfo(serialNumber).charge();
        return new Capacity(charge / 100.0);
    }

    @Override
    public boolean isCharging() throws IOException {
        return api.batteryInfo(serialNumber).isCharging();
    }

}
