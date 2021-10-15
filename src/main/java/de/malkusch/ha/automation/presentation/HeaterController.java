package de.malkusch.ha.automation.presentation;

import java.io.IOException;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.heater.HeatUpHotWaterApplicationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public final class HeaterController {

    private final HeatUpHotWaterApplicationService api;

    @PutMapping("/heater/hotWater/heatUp")
    public void heatUpHotWater() throws IOException, InterruptedException {
        api.heatUp();
    }
}
