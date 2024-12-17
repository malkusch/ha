package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.NONE;
import static de.malkusch.ha.shared.infrastructure.DateUtil.formatDuration;

import java.time.Duration;
import java.time.Instant;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterEnabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties;
import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties.ChargingRule.Balancing;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.scooter.Kilometers;
import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ScooterEnabled
public final class ChargingStrategy_2_1_StartBalancing extends ChargingStrategy {

    private static record Interval(Duration duration, Kilometers kilometers) {

        @Override
        public String toString() {
            return String.format("[%s, %s]", formatDuration(duration), kilometers);
        }
    }

    public final Interval earliest;
    public  final Interval latest;
    private final Capacity earlyBalancingStartCharge;

    @Autowired
    ChargingStrategy_2_1_StartBalancing(ScooterProperties properties) {
        this(interval(properties.getChargingRule().getBalancing().getEarliest()),
                interval(properties.getChargingRule().getBalancing().getLatest()),
                new Capacity(properties.getChargingRule().getBalancing().getEarlyStartCharge()));
    }

    private static Interval interval(Balancing.Interval properties) {
        return new Interval(properties.getInterval(), new Kilometers(properties.getKilometers()));
    }

    @Override
    public Evaluation evaluate(Context context) throws Exception {
        var lastBalancing = context.lastBalancing.lazy();
        var charge = context.charge.lazy();

        var interval = latest;
        if (charge.isGreaterThanOrEquals(earlyBalancingStartCharge)) {
            interval = earliest;
        }

        var mileage = context.mileage.lazy();
        var mileageDifference = mileage.difference(lastBalancing.mileage());
        if (mileageDifference.isGreaterThan(interval.kilometers)) {
            return start(
                    String.format("Last balancing too many kilometers: %s > %s", lastBalancing, interval.kilometers));
        }

        var nextBalancingTime = lastBalancing.time().plus(interval.duration);
        if (Instant.now().isAfter(nextBalancingTime)) {
            return start(
                    String.format("Last balancing too old: %s > %s", lastBalancing, formatDuration(interval.duration)));
        }

        return NONE;
    }

    @Override
    public String toString() {
        return String.format("%s(%s: %s - %s)", getClass().getSimpleName(), earlyBalancingStartCharge, earliest,
                latest);
    }
}
