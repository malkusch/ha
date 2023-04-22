package de.malkusch.ha.automation.model.scooter;

import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.P75;
import static de.malkusch.ha.automation.model.electricity.Watt.min;
import static de.malkusch.ha.automation.model.scooter.Scooter.State.BATTERY_DISCONNECTED;
import static de.malkusch.ha.automation.model.scooter.Scooter.State.CHARGING;
import static de.malkusch.ha.automation.model.scooter.Scooter.State.OFFLINE;

import java.time.Duration;

import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.automation.model.geo.Distance;
import de.malkusch.ha.automation.model.geo.DistanceCalculator;
import de.malkusch.ha.automation.model.scooter.Scooter.ScooterException;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.WallboxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ScooterChargingRule implements Rule {

    private final Duration evaluationRate;
    private final Capacity minimumStartCharge;
    private final Capacity minimumStopCharge;
    private final Capacity maximumCharge;
    private final Scooter scooter;
    private final ScooterWallbox wallbox;
    private final Electricity electricity;

    private final Duration excessWindow;
    private final Watt startExcess;
    private final Watt stopExcess;
    private final Capacity excessStartCharge;

    private final DistanceCalculator distanceCalculator;
    private final Distance maxDistance;

    @Override
    public void evaluate() throws Exception {
        try {
            var scooterState = scooter.state();
            if (scooterState == OFFLINE) {
                log.info("Stop charging scooter: Scooter state is offline");
                wallbox.stopCharging();
                return;
            }
            if (scooterState == BATTERY_DISCONNECTED) {
                log.info("Stop charging scooter: Battery is disconnected");
                wallbox.stopCharging();
                return;
            }

            var distance = distanceCalculator.between(wallbox.location, scooter.location());
            if (distance.isGreaterThan(maxDistance)) {
                log.info("Stop charging scooter: Scooter is too far ({})", distance);
                wallbox.stopCharging();
                return;
            }

            var charge = scooter.charge();

            if (charge.isGreaterThanOrEquals(maximumCharge)) {
                log.info("Stop charging scooter. Charge {} is greater maximum charge", charge);
                wallbox.stopCharging();
                return;
            }

            if (charge.isLessThan(minimumStartCharge)) {
                log.info("Start charging scooter. Charge {} is below minimum charge", charge);
                wallbox.startCharging();
                return;
            }

            if (charge.isLessThan(minimumStopCharge) && scooterState == CHARGING) {
                return;
            }

            var recentExcess = electricity.excessProduction(P75, excessWindow);
            var currentExcess = min(electricity.excessProduction(), recentExcess);

            if (charge.isLessThan(excessStartCharge) && currentExcess.isGreaterThan(startExcess)) {
                log.info("Start excess charging scooter at charge {} and excess {}", charge, currentExcess);
                wallbox.startCharging();
                return;
            }

            if (recentExcess.isLessThan(stopExcess)) {
                log.info("Stop excess charging scooter at charge {} and excess {}", charge, recentExcess);
                wallbox.stopCharging();
                return;
            }

        } catch (WallboxException e) {
            log.warn("Ignore scooter charging automation: {}", e.error);

        } catch (ScooterException e) {
            log.warn("Ignore scooter charging automation: {}", e.error);
        }
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
