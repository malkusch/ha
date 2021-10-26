package de.malkusch.ha.automation.infrastructure.prometheus;

import static de.malkusch.ha.automation.model.Electricity.Aggregation.MAXIMUM;
import static de.malkusch.ha.automation.model.Electricity.Aggregation.MINIMUM;
import static de.malkusch.ha.automation.model.Electricity.Aggregation.P25;
import static de.malkusch.ha.automation.model.Electricity.Aggregation.P75;
import static java.math.BigDecimal.ZERO;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.Capacity;
import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.automation.model.Watt;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
final class PrometheusElectricity implements Electricity {

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;

    private static Map<Aggregation, String> AGGREGATIONS = Map.of(//
            MINIMUM, "min_over_time(%s)", //
            P25, "quantile_over_time(0.25, %s)", //
            P75, "quantile_over_time(0.75, %s)", //
            MAXIMUM, "max_over_time(%s)" //
    );

    @Override
    public Watt excess(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var promDuration = duration.toSeconds() + "s";
        var timeSeries = String.format("clamp_min(batterie_feed_in, 0)[%s:]", promDuration);
        var query = String.format(AGGREGATIONS.get(aggregation), timeSeries);
        var result = query(query);
        log.debug("{} = {}", query, result);
        return new Watt(result.doubleValue());
    }

    @Override
    public Watt batteryConsumption(Aggregation aggregation, Duration duration)
            throws ApiException, InterruptedException {
        
        var promDuration = duration.toSeconds() + "s";
        var timeSeries = String.format("clamp_min(batterie_battery_consumption, 0)[%s:]", promDuration);
        var query = String.format(AGGREGATIONS.get(aggregation), timeSeries);
        var result = query(query);
        log.debug("{} = {}", query, result);
        return new Watt(result.doubleValue());
    }

    @Override
    public Capacity capacity() throws ApiException, InterruptedException {
        var result = query("batterie_charge");
        return new Capacity(result.doubleValue() / 100);
    }

    private BigDecimal query(String query) throws ApiException, InterruptedException {
        var url = baseUrl + "/api/v1/query?query=" + encode(query, UTF_8);
        try (var response = http.get(url)) {
            var json = mapper.readValue(response.body, Response.class);

            if (json.data.result.isEmpty()) {
                return ZERO;
            }
            return json.data.result.get(0).value.get(1);

        } catch (IOException e) {
            throw new ApiException("Faild to query " + query, e);
        }
    }

    private static class Response {
        public Data data;

        private static class Data {
            public List<Result> result;

            public static class Result {
                public List<BigDecimal> value;
            }
        }
    }
}
