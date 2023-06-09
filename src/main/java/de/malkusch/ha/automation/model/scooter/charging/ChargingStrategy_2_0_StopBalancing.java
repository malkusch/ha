package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.scooter.Scooter.State.CHARGING;
import static de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.NONE;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;

import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
final class ChargingStrategy_2_0_StopBalancing extends ChargingStrategy {

    private static final Duration MAX_BALANCE_AGE = Duration.ofDays(1);

    @Override
    public Evaluation evaluate(Context context) throws Exception {
        var charge = context.charge.lazy();
        var scooterState = context.scooterState.lazy();
        var lastBalancing = context.lastBalancing.lazy();

        var recently = Instant.now().minus(MAX_BALANCE_AGE);
        if (charge.isFull() && scooterState == CHARGING && lastBalancing.time().isAfter(recently)) {
            return stop(String.format("Balancing was finished: %s", formatTime(lastBalancing.time())));
        }

        return NONE;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
