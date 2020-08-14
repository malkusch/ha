package de.malkusch.ha.automation.model.dehumidifier;

import static de.malkusch.ha.automation.model.Electricity.Aggregation.P75;
import static de.malkusch.ha.automation.model.State.OFF;

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
public final class TurnOffDehumidifierRule implements Rule {

    private final Dehumidifier dehumidifier;
    private final Electricity electricity;
    private final Watt buffer;
    private final Duration window;
    private final Duration evaluationRate;

    @Override
    public void evaluate() throws ApiException, InterruptedException, DebounceException {
        if (dehumidifier.state() == OFF) {
            return;
        }
        var excess = electricity.excess(P75, window);
        if (excess.isLessThan(buffer)) {
            log.info("Turning off {} when p75 excess electricity was {}", dehumidifier, excess);
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
