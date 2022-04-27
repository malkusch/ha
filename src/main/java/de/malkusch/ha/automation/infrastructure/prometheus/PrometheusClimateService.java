package de.malkusch.ha.automation.infrastructure.prometheus;

import java.util.Map;

import de.malkusch.ha.automation.model.Percent;
import de.malkusch.ha.automation.model.RoomId;
import de.malkusch.ha.automation.model.climate.ClimateService;
import de.malkusch.ha.automation.model.climate.Humidity;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class PrometheusClimateService implements ClimateService {

    private final Prometheus prometheus;
    private final Map<RoomId, String> prefixMap;

    @Override
    public Humidity humidity(RoomId room) throws ApiException, InterruptedException {
        var metric = metric(room, "humidity");
        var result = prometheus.query(metric);
        return new Humidity(new Percent(result.doubleValue() / 100));
    }

    private String metric(RoomId room, String sensor) {
        var prefix = prefixMap.get(room);
        if (prefix == null) {
            throw new IllegalArgumentException(String.format("No sensor in room %s", room));
        }
        return String.format("%s_%s", prefix, sensor);
    }
}
