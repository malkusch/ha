package de.malkusch.ha.automation.application.heater;

import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.HIGH;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.OFF;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.heater.Heater;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class HotWaterApplicationService {

    private final Heater heater;

    public void heatUp() throws ApiException, InterruptedException {
        heater.switchHotWaterMode(HIGH);
    }

    public void turnOff() throws ApiException, InterruptedException {
        heater.switchHotWaterMode(OFF);
    }
}
