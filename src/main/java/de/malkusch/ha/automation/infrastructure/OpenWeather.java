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

import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus;
import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.weather.Cloudiness;
import de.malkusch.ha.automation.model.weather.Weather;
import de.malkusch.ha.automation.model.weather.WindSpeed;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class OpenWeather implements Weather {

    private final String oneCallUrl;
    private final HttpClient http;
    private final Prometheus prometheus;
    private final ObjectMapper mapper;

    @ConfigurationProperties("open-weather")
    @Component
    @Data
    public static class Properties {
        private String baseUrl;
        private String apiKey;
    }

    OpenWeather(Properties properties, HttpClient http, Prometheus prometheus, ObjectMapper mapper,
            LocationProperties locationProperties) throws IOException, InterruptedException {

        oneCallUrl = String.format(properties.baseUrl + "?lat=%s&lon=%s&appid=%s&exclude=minutely,alerts",
                locationProperties.latitude, locationProperties.longitude, properties.apiKey);

        this.http = http;
        this.prometheus = prometheus;
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

    private static record Response(Forecast current, List<Forecast> daily, List<Forecast> hourly) {
        private static record Forecast(int clouds, int dt, double wind_speed) {
            Instant instant() {
                return ofEpochSecond(dt);
            }

            Cloudiness cloudiness() {
                return new Cloudiness(clouds);
            }

            LocalDate localDate() {
                return instant().atZone(ZoneId.systemDefault()).toLocalDate();
            }

            WindSpeed windSpeed() {
                return WindSpeed.fromMps(wind_speed);
            }
        }
    }

    @Override
    public Temperature temperature() throws ApiException, InterruptedException {
        return new Temperature(prometheus.query("heater_system_sensors_temperatures_outdoor_t1"));
    }

    @Override
    public WindSpeed windspeed() throws ApiException, InterruptedException {
        return lastResponse.current.windSpeed();
    }
}
