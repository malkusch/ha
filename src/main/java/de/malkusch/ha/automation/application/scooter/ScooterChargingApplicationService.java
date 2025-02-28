package de.malkusch.ha.automation.application.scooter;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterEnabled;
import de.malkusch.ha.automation.infrastructure.socket.Socket;
import de.malkusch.ha.automation.model.geo.DistanceCalculator;
import de.malkusch.ha.automation.model.scooter.BalancingService;
import de.malkusch.ha.automation.model.scooter.Scooter;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.WallboxException;
import de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy_2_1_StartBalancing;
import de.malkusch.ha.shared.infrastructure.CoolDown.CoolDownException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
@ScooterEnabled
public final class ScooterChargingApplicationService {

    private final ScooterWallbox wallbox;
    private final BalancingService balancingService;
    private final ScooterWallbox.Api wallboxApi;
    private final Socket wallboxSocket;
    private final Scooter scooter;
    private final DistanceCalculator distanceCalculator;
    private final ChargingStrategy_2_1_StartBalancing startBalancingRule;

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
        var mileage = query(scooter::mileage);
        var lastBalancing = query(balancingService::lastBalancing);
        var earliestBalancing = query(() -> startBalancingRule.earliest);
        var latestBalancing = query(() -> startBalancingRule.latest);
        var distance = query(() -> distanceCalculator.between(wallbox.location, scooter.location()));

        return new ChargingState(wallboxOnline, socket, charging, scooterState, charge, mileage,
                new ChargingState.Balancing(lastBalancing, earliestBalancing, latestBalancing), distance);
    }

    public record ChargingState(boolean wallboxOnline, String socket, String charging, String scooterState,
                                       String charge, String mileage, Balancing balancing, String distance) {

        public record Balancing(String last, String earliest, String latest) {

        }
    }

    private static String query(Callable<Object> query) {
        try {
            return query.call().toString();
        } catch (Exception e) {
            return String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
