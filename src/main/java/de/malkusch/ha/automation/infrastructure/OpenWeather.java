package de.malkusch.ha.automation.infrastructure;

import static java.time.Instant.ofEpochSecond;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.weather.Cloudiness;
import de.malkusch.ha.automation.model.weather.Weather;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class OpenWeather implements Weather {

    private final String oneCallUrl;
    private final HttpClient http;
    private final ObjectMapper mapper;

    @ConfigurationProperties("open-weather")
    @Component
    @Data
    public static class Properties {
        private String baseUrl;
        private String latitude;
        private String longitude;
        private String apiKey;
    }

    OpenWeather(Properties properties, HttpClient http, ObjectMapper mapper) throws IOException, InterruptedException {
        oneCallUrl = String.format(properties.baseUrl + "?lat=%s&lon=%s&appid=%s&exclude=current,minutely,alerts",
                properties.latitude, properties.longitude, properties.apiKey);
        this.http = http;
        this.mapper = mapper;

        refreshResponse();
    }

    @Override
    public Cloudiness cloudiness(LocalDate date) throws ApiException {
        return lastResponse.daily.stream() //
                .filter(it -> it.localDate().equals(date)) //
                .findFirst() //
                .map(Response.Forecast::cloudiness) //
                .orElseThrow(() -> new ApiException("No Forecast for " + date));
    }

    private volatile Response lastResponse;

    @Scheduled(cron = "${open-weather.refresh-cron}")
    void refreshResponse() throws IOException, InterruptedException {
        try (var response = http.get(oneCallUrl)) {
            log.debug("Refreshing weather forecast");
            lastResponse = mapper.readValue(response.body, Response.class);
        }
    }

    private static record Response(List<Forecast> daily, List<Forecast> hourly) {
        private static record Forecast(int clouds, int dt) {
            Instant instant() {
                return ofEpochSecond(dt);
            }

            Cloudiness cloudiness() {
                return new Cloudiness(clouds);
            }

            LocalDate localDate() {
                return instant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
    }
}
