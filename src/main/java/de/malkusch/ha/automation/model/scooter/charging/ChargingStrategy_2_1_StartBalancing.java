package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.NONE;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatDuration;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties;
import de.malkusch.ha.automation.model.scooter.Kilometers;
import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
final class ChargingStrategy_2_1_StartBalancing extends ChargingStrategy {

    private final Duration balancingInterval;
    private final Kilometers maxBalancingKilometers;

    @Autowired
    ChargingStrategy_2_1_StartBalancing(ScooterProperties properties) {
        this(properties.getChargingRule().getBalancing().getInterval(),
                new Kilometers(properties.getChargingRule().getBalancing().getKilometers()));
    }

    @Override
    public Evaluation evaluate(Context context) throws Exception {
        var lastBalancing = context.lastBalancing.lazy();

        var mileage = context.mileage.lazy();
        var mileageDifference = mileage.difference(lastBalancing.mileage());
        if (mileageDifference.isGreaterThan(maxBalancingKilometers)) {
            return start(String.format("Last balancing too many kilometers: %s", lastBalancing));
        }

        var nextBalancingTime = lastBalancing.time().plus(balancingInterval);
        if (Instant.now().isAfter(nextBalancingTime)) {
            return start(String.format("Last balancing too old: %s", lastBalancing));
        }

        return NONE;
    }

    @Override
    public String toString() {
        return String.format("%s(%s, %s)", getClass().getSimpleName(), formatDuration(balancingInterval),
                maxBalancingKilometers);
    }
}
