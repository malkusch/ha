package de.malkusch.ha.automation.model.heater;

import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.P75;
import static de.malkusch.ha.automation.model.electricity.Watt.min;

import java.time.Duration;

import de.malkusch.ha.automation.infrastructure.heater.TemporaryTemperatureService;
import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.ElectricityPredictionService;
import de.malkusch.ha.automation.model.electricity.Watt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class IncreaseHeaterRule implements Rule {

    private final Duration evaluationRate;
    private final Duration window;
    private final Watt threshold;
    private final Temperature increasedTemperature;
    private final TemporaryTemperatureService temporaryTemperatureService;
    private final Electricity electricity;
    private final ElectricityPredictionService electricityPredictionService;

    @Override
    public void evaluate() throws Exception {
        if (!electricityPredictionService.predictLoadedBattery()) {
            log.debug("Don't increase, because the battery won't be loaded");
            return;
        }

        var excessProduction = min(electricity.excessProduction(P75, window), electricity.excessProduction());
        if (excessProduction.isGreaterThan(threshold)) {
            log.debug("Increasing heater when p75 excess energy production was {}", excessProduction);
            temporaryTemperatureService.changeTemporaryHeaterTemperature(increasedTemperature);
        }

        log.debug("Don't increase, because not enough excess production {}", excessProduction);
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }
}
