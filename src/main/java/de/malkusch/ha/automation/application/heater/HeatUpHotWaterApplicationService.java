package de.malkusch.ha.automation.application.heater;

import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.HIGH;

import java.io.IOException;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.heater.Heater;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class HeatUpHotWaterApplicationService {

    private final Heater heater;

    public void heatUp() throws IOException, InterruptedException {
        heater.switchHotWaterMode(HIGH);
    }
}
