package de.malkusch.ha.automation.infrastructure.prometheus;

import static de.malkusch.ha.automation.model.Electricity.Aggregation.MAXIMUM;
import static de.malkusch.ha.automation.model.Electricity.Aggregation.MINIMUM;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.automation.model.Watt;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class PrometheusElectricity implements Electricity {

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String host;

    private static Map<Aggregation, String> AGGREGATIONS = Map.of(//
            MINIMUM, "min_over_time", //
            MAXIMUM, "max_over_time" //
    );

    private static class Response {
        public Data data;

        private static class Data {
            public List<Result> result;

            public static class Result {
                public List<Integer> value;
            }
        }
    }

    @Override
    public Watt excess(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var promDuration = duration.getSeconds() + "s";
        var query = String.format(
                "%s(((batterie_production - batterie_consumption + (batterie_battery_consumption < 0)) > 0)[%s:])",
                AGGREGATIONS.get(aggregation), promDuration);
        var url = "http://" + host + "/api/v1/query?query=" + encode(query, UTF_8);
        try (var response = http.get(url)) {
            var json = mapper.readValue(response.body, Response.class);

            if (json.data.result.isEmpty()) {
                return new Watt(0);
            }
            return new Watt(json.data.result.get(0).value.get(1));

        } catch (IOException e) {
            throw new ApiException("Faild to query " + query, e);
        }
    }
}
