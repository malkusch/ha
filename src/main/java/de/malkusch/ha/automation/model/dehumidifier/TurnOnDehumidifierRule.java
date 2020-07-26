package de.malkusch.ha.automation.model.dehumidifier;

import static de.malkusch.ha.automation.model.Electricity.Aggregation.MINIMUM;
import static de.malkusch.ha.automation.model.State.ON;

import java.time.Duration;

import de.malkusch.ha.automation.infrastructure.Debouncer.DebounceException;
import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.Watt;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class TurnOnDehumidifierRule implements Rule {

    private final Dehumidifier dehumidifier;
    private final Electricity electricity;
    private final Watt buffer;
    private final Duration window;
    private final Duration evaluationRate;

    @Override
    public void evaluate() throws ApiException, InterruptedException, DebounceException {
        if (dehumidifier.state() == ON) {
            return;
        }
        var threshold = dehumidifier.power.plus(buffer);
        var excess = electricity.excess(MINIMUM, window);
        if (excess.isGreaterThan(threshold)) {
            log.info("Turning on {} at excess electricity of {}", dehumidifier, excess);
            dehumidifier.turnOn();
        }
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }

    @Override
    public String toString() {
        return String.format("TurnOn(%s) ", dehumidifier);
    }
}
