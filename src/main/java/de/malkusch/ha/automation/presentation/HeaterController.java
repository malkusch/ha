package de.malkusch.ha.automation.presentation;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.heater.HotWaterApplicationService;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public final class HeaterController {

    private final HotWaterApplicationService api;

    @PutMapping("/heater/hotWater/heatUp")
    public void heatUpHotWater() throws ApiException, InterruptedException {
        api.heatUp();
    }

    @PutMapping("/heater/hotWater/turnOff")
    public void turnOffHotWater() throws ApiException, InterruptedException {
        api.turnOff();
    }
}
