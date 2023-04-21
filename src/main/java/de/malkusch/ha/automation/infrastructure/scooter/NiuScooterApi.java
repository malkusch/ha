package de.malkusch.ha.automation.infrastructure.scooter;

import static de.malkusch.ha.automation.model.scooter.Scooter.State.BATTERY_DISCONNECTED;
import static de.malkusch.ha.automation.model.scooter.Scooter.State.CHARGING;
import static de.malkusch.ha.automation.model.scooter.Scooter.State.OFFLINE;
import static de.malkusch.ha.automation.model.scooter.Scooter.State.READY_TO_CHARGE;

import java.io.IOException;

import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.scooter.Scooter;
import de.malkusch.ha.automation.model.scooter.Scooter.State;
import de.malkusch.niu.Niu;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class NiuScooterApi implements Scooter.Api {

    private final String serialNumber;
    private final Niu api;

    @Override
    public Capacity charge() throws IOException {
        var charge = api.batteryInfo(serialNumber).charge();
        return new Capacity(charge / 100.0);
    }

    @Override
    public State state() throws IOException {
        {
            var vehicle = api.vehicle(serialNumber);
            if (vehicle.ss_online_sta() == 0) {
                return OFFLINE;
            }
            if (!vehicle.isConnected()) {
                return BATTERY_DISCONNECTED;
            }
        }
        {
            var battery = api.batteryInfo(serialNumber);
            if (battery.isCharging()) {
                return CHARGING;

            } else {
                return READY_TO_CHARGE;
            }
        }
    }
}
