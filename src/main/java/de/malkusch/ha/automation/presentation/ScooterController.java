package de.malkusch.ha.automation.presentation;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.IOException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.scooter.ScooterChargingApplicationService;
import de.malkusch.ha.automation.application.scooter.ScooterChargingApplicationService.ChargingState;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.WallboxException;
import de.malkusch.ha.shared.infrastructure.CoolDown.CoolDownException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public final class ScooterController {

    private final ScooterChargingApplicationService chargingService;

    @PutMapping("/scooter/charging/start")
    public void startCharging() throws IOException, WallboxException, CoolDownException {
        chargingService.startCharging();
    }

    @PutMapping("/scooter/charging/stop")
    public void stopCharging() throws IOException, WallboxException, CoolDownException {
        chargingService.stopCharging();
    }

    @GetMapping("/scooter/charging")
    public ChargingState state() {
        return chargingService.getChargingState();
    }

    @ExceptionHandler(WallboxException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public String apiError(WallboxException error) {
        return String.format("WallboxException: %s", error.toString());
    }
}
