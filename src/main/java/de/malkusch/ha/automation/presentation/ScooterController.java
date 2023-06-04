package de.malkusch.ha.automation.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.scooter.GetScooterChargingStateApplicationService;
import de.malkusch.ha.automation.application.scooter.GetScooterChargingStateApplicationService.ChargingState;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public final class ScooterController {

    private final GetScooterChargingStateApplicationService getChargingStateService;

    @GetMapping("/scooter/charging-state")
    public ChargingState state() {
        return getChargingStateService.getChargingState();
    }
}
