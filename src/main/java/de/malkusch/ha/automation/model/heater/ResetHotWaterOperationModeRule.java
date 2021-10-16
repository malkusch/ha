package de.malkusch.ha.automation.model.heater;

import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.HIGH;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.OFF;
import static de.malkusch.ha.automation.model.heater.Heater.HotWaterMode.OWNPROGRAM;

import java.time.Duration;

import de.malkusch.ha.automation.model.Rule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class ResetHotWaterOperationModeRule implements Rule {

    private final Heater heater;
    private final Duration evaluationRate;

    @Override
    public void evaluate() throws Exception {
        var currentMode = heater.currentHotWaterMode();
        if (currentMode == OWNPROGRAM) {
            return;
        }

        var ownProgramHotWaterMode = heater.ownProgramHotWaterMode();

        if (ownProgramHotWaterMode == currentMode) {
            heater.switchHotWaterMode(OWNPROGRAM);
            log.info("Switched heater in {} mode back to OWNPROGRAM", currentMode);

        } else if (currentMode == OFF && ownProgramHotWaterMode == HIGH) {
            heater.switchHotWaterMode(OWNPROGRAM);
            log.info("Switched heater in {} mode back to OWNPROGRAM", currentMode);
        }
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
