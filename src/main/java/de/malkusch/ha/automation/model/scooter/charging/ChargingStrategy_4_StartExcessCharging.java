package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.NONE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
final class ChargingStrategy_4_StartExcessCharging extends ChargingStrategy {

    @Autowired
    ChargingStrategy_4_StartExcessCharging(ScooterProperties properties) {
        this(new Capacity(properties.getChargingRule().getExcessCharging().getStartCharge()),
                new Watt(properties.getChargingRule().getExcessCharging().getStartExcess()));
    }

    private final Capacity startCharge;
    private final Watt startExcess;

    @Override
    public Evaluation evaluate(Context context) throws Exception {
        var currentExcess = context.currentExcess.lazy();
        var charge = context.charge.lazy();

        if (charge.isLessThan(startCharge) && currentExcess.isGreaterThan(startExcess)) {
            return start(String.format("Enough excess %s > %s; Charge low enough %s < %s", currentExcess, startExcess,
                    charge, startCharge));

        }
        return NONE;
    }

    @Override
    public String toString() {
        return String.format("%s(<%s, >%s)", getClass().getSimpleName(), startCharge, startExcess);
    }
}
