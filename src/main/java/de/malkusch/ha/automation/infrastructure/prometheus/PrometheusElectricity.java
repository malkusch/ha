package de.malkusch.ha.automation.infrastructure.prometheus;

import static de.malkusch.ha.automation.model.Electricity.Aggregation.MAXIMUM;
import static de.malkusch.ha.automation.model.Electricity.Aggregation.MINIMUM;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

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
    private final Duration delay;

    private static Map<Aggregation, String> AGGREGATIONS = Map.of(//
            MINIMUM, "min_over_time", //
            MAXIMUM, "max_over_time" //
    );

    @Override
    public Watt excess(Aggregation aggregation, Duration duration) throws ApiException, InterruptedException {
        var promDuration = duration.toSeconds() + "s";
        var query = String.format(
                "%s(clamp_min((batterie_production - batterie_consumption + clamp_max(batterie_battery_consumption, 0)), 0)[%s:])",
                AGGREGATIONS.get(aggregation), promDuration);
        var result = query(query);
        log.debug("{} = {}", query, result);
        return new Watt(result);
    }

    private int query(String query) throws ApiException, InterruptedException {
        delay();
        var url = baseUrl + "/api/v1/query?query=" + encode(query, UTF_8);
        try (var response = http.get(url)) {
            var json = mapper.readValue(response.body, Response.class);

            if (json.data.result.isEmpty()) {
                return 0;
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
                public List<Integer> value;
            }
        }
    }

    private Instant delayUntil = now();

    private void delay() throws InterruptedException {
        for (var now = now(); now.isBefore(delayUntil); now = now()) {
            MILLISECONDS.sleep(Duration.between(now, delayUntil).toMillis());
        }
        delayUntil = now().plus(delay);
    }
}
