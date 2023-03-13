package de.malkusch.ha.automation.infrastructure.prometheus;

import static de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.AggregationQuery.Aggregation.P95;
import static java.util.Optional.empty;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.Query;
import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus.SimpleQuery;
import de.malkusch.ha.automation.model.Percent;
import de.malkusch.ha.automation.model.climate.CO2;
import de.malkusch.ha.automation.model.climate.ClimateService;
import de.malkusch.ha.automation.model.climate.Dust;
import de.malkusch.ha.automation.model.climate.Dust.PM2_5;
import de.malkusch.ha.automation.model.climate.Humidity;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class PrometheusClimateService implements ClimateService {

    private final Prometheus prometheus;
    private final String outsidePrefix;
    private final Map<RoomId, String> prefixMap;

    @Override
    public Humidity humidity(RoomId room) throws ApiException, InterruptedException {
        var metric = metric(room, "humidity");
        var result = prometheus.query(metric.get());
        return new Humidity(new Percent(result.doubleValue() / 100));
    }

    @Override
    public Optional<Dust> dust(RoomId room) throws ApiException, InterruptedException {
        return query(metric(room, "pm25")) //
                .map(PM2_5::new) //
                .map(Dust::new);
    }

    @Override
    public Dust outsideDust() throws ApiException, InterruptedException {
        var query = new SimpleQuery("%s_%s", outsidePrefix, "pm25");
        var result = prometheus.query(query);
        return new Dust(new PM2_5(result));
    }

    private static final Duration RECENTLY = Duration.ofMinutes(5);

    @Override
    public Optional<CO2> co2(RoomId room) throws ApiException, InterruptedException {
        var query = metric(room, "co2") //
                .map(it -> it.subquery(RECENTLY)) //
                .map(it -> it.aggregate(P95));

        return query(query) //
                .map(it -> new CO2(it.intValue()));
    }

    private Optional<BigDecimal> query(Optional<? extends Query> query) throws ApiException, InterruptedException {
        if (query.isEmpty()) {
            return empty();
        }
        return Optional.of(prometheus.query(query.get()));
    }

    private Optional<Query> metric(RoomId room, String sensor) {
        return Optional.ofNullable(prefixMap.get(room)) //
                .map(prefix -> new SimpleQuery("%s_%s", prefix, sensor));
    }
}
