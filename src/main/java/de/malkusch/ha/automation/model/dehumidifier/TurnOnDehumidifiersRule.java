package de.malkusch.ha.automation.model.dehumidifier;

import static de.malkusch.ha.automation.model.Electricity.Aggregation.P25;
import static de.malkusch.ha.automation.model.State.OFF;

import java.time.Duration;

import de.malkusch.ha.automation.infrastructure.Debouncer.DebounceException;
import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.Watt;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
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

    @Override
    public void evaluate() throws ApiException, InterruptedException, DebounceException {
        var dehumidifier = findNext();
        if (dehumidifier == null) {
            return;
        }

        var threshold = dehumidifier.power.plus(buffer);
        var excess = electricity.excess(P25, window);
        if (excess.isGreaterThan(threshold)) {
            log.info("Turning on {} when p25 excess electricity was {}", dehumidifier, excess);
            dehumidifier.turnOn();
        }
    }

    private Dehumidifier findNext() throws ApiException, InterruptedException {
        for (var canidate : dehumidifiers.findAll()) {
            if (canidate.state() == OFF) {
                return canidate;
            }
        }
        return null;
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
