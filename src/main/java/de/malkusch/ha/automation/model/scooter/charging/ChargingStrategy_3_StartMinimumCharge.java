package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.scooter.Scooter.State.CHARGING;
import static de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.NONE;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
final class ChargingStrategy_3_StartMinimumCharge extends ChargingStrategy {

    @Autowired
    ChargingStrategy_3_StartMinimumCharge(ScooterProperties properties) {
        this(new Capacity(properties.getChargingRule().getMinimumCharge().getStart()),
                new Capacity(properties.getChargingRule().getMinimumCharge().getStop()));
    }

    private final Capacity minimumStartCharge;
    private final Capacity minimumStopCharge;

    @Override
    public Evaluation evaluate(Context context) throws Exception {
        var charge = context.charge.lazy();
        var scooterState = context.scooterState.lazy();

        if (charge.isLessThan(minimumStartCharge)) {
            return start(String.format("Charge %s is below minimum charge %s", charge, minimumStartCharge));

        } else if (charge.isLessThan(minimumStopCharge) && scooterState == CHARGING) {
            return start(String.format("Charge %s is below minimum stop charge %s", charge, minimumStopCharge));

        } else {
            return NONE;
        }
    }

    @Override
    public String toString() {
        return String.format("%s(<[%s; %s])", getClass().getSimpleName(), minimumStartCharge, minimumStopCharge);
    }
}
