package de.malkusch.ha.automation.model.dehumidifier;

import static de.malkusch.ha.automation.model.State.ON;
import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.P75;
import static de.malkusch.ha.automation.model.electricity.Watt.min;

import java.time.Duration;

import de.malkusch.ha.automation.infrastructure.Debouncer.DebounceException;
import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.climate.ClimateService;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class TurnOnDehumidifiersRule implements Rule {

    private final DehumidifierRepository dehumidifiers;
    private final Electricity electricity;
    private final Watt buffer;
    private final Duration window;
    private final Duration evaluationRate;
    private final ClimateService climateService;

    @Override
    public void evaluate() throws ApiException, InterruptedException, DebounceException {
        for (var dehumidifier : dehumidifiers.findAll()) {
            if (dehumidifier.state() == ON) {
                continue;
            }

            var humidity = climateService.humidity(dehumidifier.room);
            if (humidity.isLessThan(dehumidifier.desiredHumidity.maximum())) {
                continue;
            }

            var threshold = dehumidifier.power.plus(buffer);
            var excess = min(electricity.excess(P75, window), electricity.excess());
            if (excess.isGreaterThan(threshold)) {
                log.info("Turning on {}: Excess electricity was {} greater than {}", dehumidifier, excess, threshold);
                dehumidifier.turnOn();
                return;
            }
        }
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }

    @Override
    public String toString() {
        return String.format("%s(±%s)", getClass().getSimpleName(), buffer);
    }
}
