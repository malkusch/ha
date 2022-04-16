package de.malkusch.ha.automation.model.dehumidifier;

import static de.malkusch.ha.automation.model.State.ON;
import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.P75;

import java.time.Duration;

import de.malkusch.ha.automation.infrastructure.Debouncer.DebounceException;
import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public final class TurnOffDehumidifiersRule implements Rule {

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

        var excess = electricity.excess(P75, window);
        if (excess.isLessThan(buffer)) {
            log.info("Turning off {} when p75 excess electricity was {}", dehumidifier, excess);
            dehumidifier.turnOff();
        }
    }

    private Dehumidifier findNext() throws ApiException, InterruptedException {
        for (var canidate : dehumidifiers.findAll()) {
            if (canidate.state() == ON) {
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
