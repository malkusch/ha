package de.malkusch.ha.automation.infrastructure.prometheus;

import static de.malkusch.ha.automation.model.electricity.Capacity.FULL;
import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.MAXIMUM;
import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.MINIMUM;
import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.P25;
import static de.malkusch.ha.automation.model.electricity.Electricity.Aggregation.P75;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
final class PrometheusElectricity implements Electricity {

    private final Prometheus prometheus;

    private static final Map<Aggregation, String> AGGREGATIONS = Map.of(//
            MINIMUM, "min_over_time(%s)", //
            P25, "quantile_over_time(0.25, %s)", //
            P75, "quantile_over_time(0.75, %s)", //
            MAXIMUM, "max_over_time(%s)" //
    );

    @Override
    public Watt excess(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var query = aggregation("clamp_min(batterie_feed_in, 0)", duration, aggregation);
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    @Override
    public Watt excess() throws ApiException, InterruptedException {
        var query = "clamp_min(batterie_feed_in, 0)";
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    @Override
    public Watt excessProduction(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var query = aggregation("clamp_min(batterie_production - batterie_consumption, 0)", duration, aggregation);
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    @Override
    public Watt excessProduction() throws ApiException, InterruptedException {
        var query = "clamp_min(batterie_production - batterie_consumption, 0)";
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    @Override
    public Watt consumption(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var query = aggregation("batterie_consumption", duration, aggregation);
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    @Override
    public Watt production(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var query = aggregation("batterie_production", duration, aggregation);
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    @Override
    public Watt batteryConsumption(Aggregation aggregation, Duration duration)
            throws ApiException, InterruptedException {

        var query = aggregation("clamp_min(batterie_battery_consumption, 0)", duration, aggregation);
        var result = prometheus.query(query);
        return new Watt(result.doubleValue());
    }

    @Override
    public Capacity capacity() throws ApiException, InterruptedException {
        var result = prometheus.query("batterie_charge");
        return capacity(result);
    }

    private static Capacity capacity(BigDecimal result) {
        return new Capacity(result.doubleValue() / 100);
    }

    private static final Duration ONE_DAY = Duration.ofDays(1);

    @Override
    public boolean wasFullyCharged(LocalDate date) throws ApiException, InterruptedException {
        var query = aggregation("batterie_charge", ONE_DAY, MAXIMUM);
        var result = prometheus.query(query, date);
        var max = capacity(result);
        return max.equals(FULL);
    }

    private static String aggregation(String query, Duration duration, Aggregation aggregation) {
        var timeSeries = timeSeries(query, duration);
        return String.format(AGGREGATIONS.get(aggregation), timeSeries);
    }

    private static String timeSeries(String query, Duration duration) {
        var promDuration = duration.toSeconds() + "s";
        return String.format("%s[%s:]", query, promDuration);
    }
}
