package de.malkusch.ha.automation.infrastructure.prometheus;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

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
        return query(metric(room, "pm25")).map(it -> new Dust(new PM2_5(it.doubleValue())));
    }

    @Override
    public Dust outsideDust() throws ApiException, InterruptedException {
        var result = prometheus.query(String.format("%s_%s", outsidePrefix, "pm25"));
        return new Dust(new PM2_5(result.doubleValue()));
    }

    @Override
    public Optional<CO2> co2(RoomId room) throws ApiException, InterruptedException {
        var query = metric(room, "co2").map(it -> String.format("quantile_over_time(0.95, %s[5m])", it));
        return query(query).map(it -> new CO2(it.intValue()));
    }

    private Optional<BigDecimal> query(Optional<String> query) throws ApiException, InterruptedException {
        if (query.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(prometheus.query(query.get()));
    }

    private Optional<String> metric(RoomId room, String sensor) {
        return Optional.ofNullable(prefixMap.get(room)).map(prefix -> String.format("%s_%s", prefix, sensor));
    }
}
