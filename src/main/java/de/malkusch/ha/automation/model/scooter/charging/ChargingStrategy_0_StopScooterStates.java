package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.NONE;

import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
final class ChargingStrategy_0_StopScooterStates extends ChargingStrategy {

    @Override
    public Evaluation evaluate(Context context) throws Exception {
        return switch (context.scooterState.lazy()) {
        case OFFLINE -> stop("Scooter is offline");
        case BATTERY_DISCONNECTED -> stop("Battery is disconnected");
        default -> NONE;
        };
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
