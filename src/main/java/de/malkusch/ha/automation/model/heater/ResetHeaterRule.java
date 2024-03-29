package de.malkusch.ha.automation.model.heater;

import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.P75;

import java.time.Duration;

import de.malkusch.ha.automation.infrastructure.heater.TemporaryTemperatureService;
import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class ResetHeaterRule implements Rule {

    private final Duration evaluationRate;
    private final Duration window;
    private final Watt threshold;
    private final TemporaryTemperatureService temporaryTemperatureService;
    private final Electricity electricity;

    @Override
    public void evaluate() throws Exception {
        var excessProduction = electricity.excessProduction(P75, window);
        if (excessProduction.isLessThan(threshold)) {
            log.debug("Resetting heater: Excess production ({}) was less than {}", excessProduction, threshold);
            temporaryTemperatureService.resetTemporaryHeaterTemperature();
        }
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }

    @Override
    public String toString() {
        return String.format("%s(<%s)", getClass().getSimpleName(), threshold);
    }
}
