package de.malkusch.ha.automation.model.scooter.charging;

import static de.malkusch.ha.automation.model.scooter.charging.ChargingStrategy.Evaluation.NONE;
import static lombok.AccessLevel.PRIVATE;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterEnabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.scooter.ScooterConfiguration.ScooterProperties;
import de.malkusch.ha.automation.model.geo.Distance;
import de.malkusch.ha.automation.model.geo.DistanceCalculator;
import de.malkusch.ha.automation.model.scooter.Scooter;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox;
import de.malkusch.ha.automation.model.scooter.charging.ContextFactory.Context;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ScooterEnabled
final class ChargingStrategy_1_StopScooterNotNearWallbox extends ChargingStrategy {

    private final Scooter scooter;
    private final ScooterWallbox wallbox;
    private final DistanceCalculator distanceCalculator;
    private final Distance maxDistance;

    @Autowired
    public ChargingStrategy_1_StopScooterNotNearWallbox(Scooter scooter, ScooterWallbox wallbox,
            DistanceCalculator distanceCalculator, ScooterProperties properties) {

        this(scooter, wallbox, distanceCalculator, new Distance(properties.getChargingRule().getMaximumDistance()));
    }

    @Override
    public Evaluation evaluate(Context context) throws Exception {
        var distance = distanceCalculator.between(wallbox.location, scooter.location());

        if (distance.isGreaterThan(maxDistance)) {
            return stop("Scooter is too far: " + distance);
        } else {
            return NONE;
        }
    }

    @Override
    public String toString() {
        return String.format("%s(>%s)", getClass().getSimpleName(), maxDistance);
    }
}
