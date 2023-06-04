package de.malkusch.ha.automation.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.heater.HeaterApplicationService;
import de.malkusch.ha.automation.application.heater.HeaterApplicationService.HeaterState;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public final class HeaterController {

    private final HeaterApplicationService heaterService;

    @GetMapping("/heater")
    public HeaterState heater() {
        return heaterService.heaterState();
    }
}
