package de.malkusch.ha.automation.infrastructure.prometheus;

import static de.malkusch.ha.shared.infrastructure.DateUtil.toTimestamp;
import static java.math.BigDecimal.ZERO;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class PrometheusHttpClient implements Prometheus {

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;

    public BigDecimal query(String query, LocalDate date) throws ApiException, InterruptedException {
        var url = baseUrl + "/api/v1/query?query=" + encode(query, UTF_8);

        if (date != null) {
            var start = toTimestamp(date);
            url += "&start=" + start;
        }

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
