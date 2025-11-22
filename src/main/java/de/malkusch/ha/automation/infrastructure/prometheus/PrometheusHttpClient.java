package de.malkusch.ha.automation.infrastructure.prometheus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static de.malkusch.ha.shared.infrastructure.DateUtil.toTimestamp;
import static java.math.BigDecimal.ZERO;
import static java.nio.charset.StandardCharsets.UTF_8;

@RequiredArgsConstructor
@Slf4j
public final class PrometheusHttpClient implements Prometheus {

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String baseUrl;

    @Override
    public BigDecimal query(Query query, Instant time) throws ApiException, InterruptedException {
        return query(query, time.getEpochSecond());
    }

    @Override
    public BigDecimal query(Query query, LocalDate end) throws ApiException, InterruptedException {
        return query(query, toTimestamp(end));
    }

    @Override
    public BigDecimal query(Query query) throws ApiException, InterruptedException {
        return query(query, 0);
    }

    private BigDecimal query(Query query, long time) throws ApiException, InterruptedException {
        var promQL = query.promQL();
        log.debug("Query{}: {}", time == 0 ? "" : ("@" + time), promQL);
        var url = baseUrl + "/api/v1/query?query=" + encode(promQL);

        if (time != 0) {
            url += "&time=" + time;
        }

        try (var response = http.get(url)) {
            var json = mapper.readValue(response.body, Response.class);

            if (json.data.result.isEmpty()) {
                return ZERO;
            }
            return json.data.result.get(0).value.get(1);

        } catch (IOException e) {
            throw new ApiException("Failed to query " + query, e);
        }
    }

    public static String encode(String url) {
        return URLEncoder.encode(url, UTF_8);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Response {
        public Data data;

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Data {
            public List<Result> result;

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Result {
                public List<BigDecimal> value;
            }
        }
    }
}
