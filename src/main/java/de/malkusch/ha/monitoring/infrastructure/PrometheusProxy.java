package de.malkusch.ha.monitoring.infrastructure;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.scheduling.annotation.Scheduled;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import io.prometheus.client.Gauge;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PrometheusProxy {

    private final String url;
    private final HttpClient http;
    private final ObjectMapper mapper;
    private final Collection<Mapping> mappings;

    @RequiredArgsConstructor
    public static final class Mapping {
        private final String jsonPath;
        private final Gauge gauge;
    }

    public static Mapping mapping(String jsonPath, String prometheusName) {
        var gauge = Gauge.build().name(prometheusName).help(prometheusName).create();
        gauge.register();
        return new Mapping(jsonPath, gauge);
    }

    @PostConstruct
    @Scheduled(fixedRateString = "${monitoring.updateRate}")
    public void update() throws IOException, InterruptedException {
        try (var response = http.get(url)) {
            var json = mapper.readTree(response.body);
            for (var mapping : mappings) {
                var value = json.at(mapping.jsonPath).asDouble();
                mapping.gauge.set(value);
            }
        }
    }
}
