package de.malkusch.ha.automation.model.dehumidifier;

import static de.malkusch.ha.automation.model.Electricity.Aggregation.MAXIMUM;
import static de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.State.OFF;

import java.time.Duration;

import de.malkusch.ha.automation.infrastructure.Debouncer.DebounceException;
import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.automation.model.Rule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class TurnOffDehumidifierRule implements Rule {

    private final Dehumidifier dehumidifier;
    private final Electricity electricity;
    private final Duration window;
    private final Duration evaluationRate;

    @Override
    public void evaluate() throws ApiException, InterruptedException, DebounceException {
        if (dehumidifier.state() == OFF) {
            return;
        }
        var excess = electricity.excess(MAXIMUM, window);
        if (excess.isZero()) {
            log.info("Turning off {} when no excess electricity was left", dehumidifier);
            dehumidifier.turnOff();
        }
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }

    @Override
    public String toString() {
        return String.format("TurnOff(%s) ", dehumidifier);
    }
}
