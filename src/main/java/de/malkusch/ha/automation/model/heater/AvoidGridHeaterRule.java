package de.malkusch.ha.automation.model.heater;

import static de.malkusch.ha.automation.model.Electricity.Aggregation.P75;
import static de.malkusch.ha.automation.model.heater.Heater.HeaterProgram.DAY;
import static de.malkusch.ha.automation.model.heater.Heater.HeaterProgram.NIGHT;

import java.time.Duration;

import de.malkusch.ha.automation.model.Capacity;
import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.Watt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class AvoidGridHeaterRule implements Rule {

    private final Capacity minCapacity;
    private final Watt excessThreshold;
    private final Duration evaluationRate;
    private final Electricity electricity;
    private final Heater heater;
    private final TemporaryDayTemperatureService temperatureService;

    @Override
    public void evaluate() throws Exception {
        if (heater.currentHeaterProgram() != DAY) {
            temperatureService.reset();
            return;
        }
        try {
            var evaluationPeriod = evaluationRate.dividedBy(2);

            if (electricity.capacity().isLessThan(minCapacity)) {
                temperatureService.stepMin();
                return;
            }

            if (!electricity.batteryConsumption(P75, evaluationPeriod).isZero()) {
                temperatureService.stepDown();
                return;
            }

            var production = electricity.production(P75, evaluationPeriod);
            var consumption = electricity.consumption(P75, evaluationPeriod);
            if (production.isGreaterThan(consumption)) {
                var excess = production.minus(consumption);
                if (excess.isGreaterThan(excessThreshold)) {
                    log.info("Stepping up heater");
                    temperatureService.stepUp();
                    return;
                }
            }

        } finally {
            // In case the rule was just evaluated right before the night switch
            // happened
            if (heater.currentHeaterProgram() == NIGHT) {
                temperatureService.reset();
            }
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
