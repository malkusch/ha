package de.malkusch.ha.automation.infrastructure.prometheus;

import static de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.AggregationQuery.Aggregation.COUNT;
import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.MAXIMUM;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.AggregationQuery;
import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.Query;
import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.SimpleQuery;
import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.Subquery;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class PrometheusElectricity implements Electricity {

    private final Prometheus prometheus;

    PrometheusElectricity(Prometheus prometheus, Capacity fullyCharged) {
        this.prometheus = prometheus;

        log.info("Fully charged battery threshold is {}", fullyCharged);
        this.fullyCharged = fullyCharged;
    }

    private static final Query EXCESS = new SimpleQuery("clamp_min(batterie_feed_in, 0)");

    @Override
    public Watt excess(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var query = EXCESS //
                .subquery(duration) //
                .aggregate(aggregation(aggregation));

        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    @Override
    public Watt excess() throws ApiException, InterruptedException {
        var result = prometheus.query(EXCESS);
        return new Watt(result.doubleValue());
    }

    private static final Query EXCESS_PRODUCTION = new SimpleQuery(
            "clamp_min(batterie_production - batterie_consumption, 0)");

    @Override
    public Watt excessProduction(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var query = EXCESS_PRODUCTION //
                .subquery(duration) //
                .aggregate(aggregation(aggregation));

        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    @Override
    public Watt excessProduction() throws ApiException, InterruptedException {
        var result = prometheus.query(EXCESS_PRODUCTION);
        return new Watt(result.doubleValue());
    }

    private static final Query CONSUMPTION = new SimpleQuery("batterie_consumption");

    @Override
    public Watt consumption(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var query = CONSUMPTION //
                .subquery(duration) //
                .aggregate(aggregation(aggregation));
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    private static final Query BATTERY_CONSUMPTION = new SimpleQuery("clamp_min(batterie_battery_consumption, 0)");

    @Override
    public Watt batteryConsumption(Aggregation aggregation, Duration duration)
            throws ApiException, InterruptedException {

        var query = BATTERY_CONSUMPTION //
                .subquery(duration) //
                .aggregate(aggregation(aggregation));
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    private static final Query PRODUCTION = new SimpleQuery("batterie_production");

    @Override
    public Watt production(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var query = PRODUCTION //
                .subquery(duration) //
                .aggregate(aggregation(aggregation));
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    private static final Query CAPACITY = new SimpleQuery("batterie_charge");

    @Override
    public Capacity capacity() throws ApiException, InterruptedException {
        var result = prometheus.query(CAPACITY);
        return capacity(result);
    }

    private static Capacity capacity(BigDecimal result) {
        return new Capacity(result.doubleValue() / 100);
    }

    private static final Duration ONE_DAY = Duration.ofDays(1);
    private final Capacity fullyCharged;

    @Override
    public boolean wasFullyCharged(LocalDate date) throws ApiException, InterruptedException {
        var query = CAPACITY //
                .subquery(ONE_DAY) //
                .aggregate(aggregation(MAXIMUM));
        var result = prometheus.query(query, date.plusDays(1));
        var max = capacity(result);
        return max.isGreaterThanOrEquals(fullyCharged);
    }

    @Override
    public boolean isConsumptionDuringProductionGreaterThan(LocalDate date, Watt threshold, Duration duration)
            throws ApiException, InterruptedException {

        var query = new SimpleQuery("(batterie_consumption > %.2f and batterie_production > 0)", threshold.getValue()) //
                .subquery(ONE_DAY, duration);
        return count(query, date) > 0;
    }

    private int count(Subquery timeSeries, LocalDate date) throws ApiException, InterruptedException {
        var query = timeSeries.aggregate(COUNT);
        var result = prometheus.query(query, date.plusDays(1));
        return result.intValue();
    }

    private static AggregationQuery.Aggregation aggregation(Aggregation aggregation) {
        return switch (aggregation) {
        case MAXIMUM -> AggregationQuery.Aggregation.MAXIMUM;
        case MINIMUM -> AggregationQuery.Aggregation.MINIMUM;
        case P25 -> AggregationQuery.Aggregation.P25;
        case P75 -> AggregationQuery.Aggregation.P75;
        };
    }
}
