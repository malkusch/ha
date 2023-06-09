package de.malkusch.ha.automation.infrastructure.scooter;

import static de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.AggregationQuery.Aggregation.MAXIMUM;
import static de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.AggregationQuery.Aggregation.MINIMUM;
import static de.malkusch.ha.automation.model.scooter.BalancingService.Balancing.NEVER;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus;
import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.Query;
import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.SimpleQuery;
import de.malkusch.ha.automation.model.scooter.BalancingService;
import de.malkusch.ha.automation.model.scooter.BalancingService.Balancing;
import de.malkusch.ha.automation.model.scooter.Mileage;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class PrometheusBalancingApi implements BalancingService.Api {

    private final Prometheus prometheus;
    private final Configuration configuration;

    static record Configuration(Duration balancingDuration, Duration chargingDuration, Duration searchWindow) {
    }

    private static final Duration RESOLUTION = Duration.ofMinutes(5);

    @Override
    public Balancing lastBalancing() throws IOException {
        try {
            var charge = new SimpleQuery("niu_Markus_battery_charge")
                    .subquery(configuration.balancingDuration, RESOLUTION).aggregate(MINIMUM);

            var charging = new SimpleQuery("niu_Markus_battery_isCharging")
                    .subquery(configuration.chargingDuration, RESOLUTION).aggregate(MINIMUM);

            var query = new SimpleQuery("(%s * %s) == 100", charge.promQL(), charging.promQL()) //
                    .query("timestamp(%s)") //
                    .subquery(configuration.searchWindow, configuration.chargingDuration) //
                    .aggregate(MAXIMUM);

            var result = prometheus.query(query);

            var timeSeconds = result.intValue();
            if (timeSeconds == 0) {
                return NEVER;
            }
            var time = Instant.ofEpochSecond(timeSeconds);
            var mileage = mileage(time);

            return new Balancing(time, mileage);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Failed to query lastBalancing", e);

        } catch (ApiException e) {
            throw new IOException("Failed to query lastBalancing", e);
        }
    }

    private static final Query MILEAGE = new SimpleQuery("niu_Markus_odometer_mileage");

    private Mileage mileage(Instant time) throws IOException, ApiException, InterruptedException {
        var query = MILEAGE;
        var result = prometheus.query(query, time);
        var kilometers = result.doubleValue();
        if (kilometers == 0) {
            return Mileage.MIN;
        }
        return new Mileage(kilometers);
    }
}
