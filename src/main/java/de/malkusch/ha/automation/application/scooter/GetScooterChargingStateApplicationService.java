package de.malkusch.ha.automation.application.scooter;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.socket.Socket;
import de.malkusch.ha.automation.model.scooter.Scooter;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class GetScooterChargingStateApplicationService {

    private final ScooterWallbox wallbox;
    private final ScooterWallbox.Api wallboxApi;
    private final Socket wallboxSocket;
    private final Scooter scooter;

    public ChargingState getChargingState() {
        var wallboxOnline = wallboxApi.isOnline();
        var socket = wallboxSocket.toString();
        var charging = query(() -> wallbox.isCharging() ? "CHARGING" : "NOT_CHARGING");
        var scooterState = query(scooter::state);
        var charge = query(scooter::charge);

        return new ChargingState(wallboxOnline, socket, charging, scooterState, charge);
    }

    public static record ChargingState(boolean wallboxOnline, String socket, String charging, String scooterState,
            String charge) {
    }

    private static String query(Callable<Object> query) {
        try {
            return query.call().toString();
        } catch (Exception e) {
            return String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
