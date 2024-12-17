package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.NONE;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterEnabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ScooterEnabled
final class ChargingStrategy_6_StopExcessCharging extends ChargingStrategy {

    @Autowired
    ChargingStrategy_6_StopExcessCharging(ScooterProperties properties) {
        this(new Watt(properties.getChargingRule().getExcessCharging().getStopExcess()));
    }

    private final Watt stopExcess;

    @Override
    public Evaluation evaluate(Context context) throws Exception {
        var recentExcess = context.recentExcess.lazy();

        if (recentExcess.isLessThan(stopExcess)) {
            return stop(String.format("Excess %s is too low", recentExcess));
        }
        return NONE;
    }

    @Override
    public String toString() {
        return String.format("%s(<%s)", getClass().getSimpleName(), stopExcess);
    }
}
