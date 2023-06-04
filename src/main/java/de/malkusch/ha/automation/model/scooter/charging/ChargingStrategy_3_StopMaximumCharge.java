package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.NONE;
import static lombok.AccessLevel.PRIVATE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
final class ChargingStrategy_3_StopMaximumCharge extends ChargingStrategy {

    @Autowired
    ChargingStrategy_3_StopMaximumCharge(ScooterProperties properties) {
        this(new Capacity(properties.getChargingRule().getMaximumCharge()));
    }

    private final Capacity maximumCharge;

    @Override
    public Evaluation evaluate(Context context) throws Exception {
        var charge = context.charge.lazy();
        if (charge.isGreaterThanOrEquals(maximumCharge)) {
            return stop(String.format("Charge %s is greater than maximum charge %s", charge, maximumCharge));

        }
        return NONE;
    }

    @Override
    public String toString() {
        return String.format("%s(>=%s)", getClass().getSimpleName(), maximumCharge);
    }
}
