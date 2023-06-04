package de.malkusch.ha.automation.application.scooter;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.socket.Socket;
import de.malkusch.ha.automation.model.geo.DistanceCalculator;
import de.malkusch.ha.automation.model.scooter.BalancingService;
import de.malkusch.ha.automation.model.scooter.Scooter;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.WallboxException;
import de.malkusch.ha.shared.infrastructure.CoolDown.CoolDownException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class ScooterChargingApplicationService {

    private final ScooterWallbox wallbox;
    private final BalancingService balancingService;
    private final ScooterWallbox.Api wallboxApi;
    private final Socket wallboxSocket;
    private final Scooter scooter;
    private final DistanceCalculator distanceCalculator;

    public void startCharging() throws IOException, WallboxException, CoolDownException {
        wallbox.startCharging();
    }

    public void stopCharging() throws IOException, WallboxException, CoolDownException {
        wallbox.stopCharging();
    }

    public ChargingState getChargingState() {
        var wallboxOnline = wallboxApi.isOnline();
        var socket = wallboxSocket.toString();
        var charging = query(() -> wallbox.isCharging() ? "CHARGING" : "NOT_CHARGING");
        var scooterState = query(scooter::state);
        var charge = query(scooter::charge);
        var lastBalancing = query(balancingService::lastBalancing);
        var distance = query(() -> distanceCalculator.between(wallbox.location, scooter.location()));

        return new ChargingState(wallboxOnline, socket, charging, scooterState, charge, lastBalancing, distance);
    }

    public static record ChargingState(boolean wallboxOnline, String socket, String charging, String scooterState,
            String charge, String lastBalancing, String distance) {
    }

    private static String query(Callable<Object> query) {
        try {
            return query.call().toString();
        } catch (Exception e) {
            return String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
