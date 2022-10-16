package de.malkusch.ha.automation.infrastructure;

import static de.malkusch.ha.shared.infrastructure.DateUtil.toTimestamp;
import static de.malkusch.ha.shared.infrastructure.event.EventPublisher.publishSafely;
import static java.time.Instant.ofEpochSecond;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.weather.Cloudiness;
import de.malkusch.ha.automation.model.weather.Weather;
import de.malkusch.ha.automation.model.weather.WindSpeed;
import de.malkusch.ha.automation.model.weather.WindSpeedChanged;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class OpenWeather implements Weather {

    private final String baseUrl;
    private final String query;
    private final HttpClient http;
    private final ObjectMapper mapper;
    private final LocalTime daylightStart;
    private final LocalTime daylightEnd;
    private Map<LocalTime, Response.Forecast> todaysHourlyForecastsMap = new ConcurrentHashMap<>();

    @ConfigurationProperties("open-weather")
    @Component
    @Data
    public static class Properties {
        private String baseUrl;
        private String apiKey;
        private String daylightStart;
        private String daylightEnd;
    }

    OpenWeather(Properties properties, HttpClient http, ObjectMapper mapper, LocationProperties locationProperties)
            throws IOException, InterruptedException {

        baseUrl = properties.baseUrl;
        query = String.format("?lat=%s&lon=%s&appid=%s&exclude=minutely,alerts", locationProperties.latitude,
                locationProperties.longitude, properties.apiKey);

        this.http = http;
        this.mapper = mapper;

        this.daylightStart = LocalTime.parse(properties.daylightStart);
        this.daylightEnd = LocalTime.parse(properties.daylightEnd);
        if (daylightEnd.isBefore(daylightStart)) {
            throw new IllegalArgumentException(
                    String.format("daylightEnd %s must be after daylightStart %s", daylightEnd, daylightStart));
        }

        lastResponse = downloadCurrentResponse();
        updateTodaysHourlyForecastsMap();
    }

    @Override
    public Cloudiness averageDaylightCloudiness() throws ApiException {
        return averageDaylightCloudiness(todaysHourlyForecastsMap.values()).orElseGet(() -> {
            log.warn("Falling back to daily forcast");
            return lastResponse.current.cloudiness();
        });
    }

    @Override
    public Cloudiness averageDaylightCloudiness(LocalDate date) throws ApiException, InterruptedException {
        try {
            return averageDaylightCloudiness(downloadPastResponse(date).hourly)
                    .orElseThrow(() -> new ApiException("Couldn't fetch average cloudiness for " + date));

        } catch (IOException e) {
            throw new ApiException("Couldn't fetch average cloudiness for " + date, e);
        }
    }

    private Optional<Cloudiness> averageDaylightCloudiness(Collection<Response.Forecast> hourlies) throws ApiException {
        var average = hourlies.stream()
                .filter(it -> it.time().isAfter(daylightStart) && it.time().isBefore(daylightEnd)).map(it -> it.clouds)
                .mapToInt(it -> it).average();

        if (average.isPresent()) {
            return Optional.of(new Cloudiness((int) average.getAsDouble()));
        } else {
            return Optional.empty();
        }
    }

    private volatile Response lastResponse;

    @Scheduled(cron = "${open-weather.current.refresh-cron}")
    void refreshResponseAndPublishWeatherEvents() throws IOException, InterruptedException {
        var lastWind = windspeed();
        lastResponse = downloadCurrentResponse();
        updateTodaysHourlyForecastsMap();

        var currentWind = windspeed();
        if (!lastWind.equals(currentWind)) {
            publishSafely(new WindSpeedChanged(currentWind));
        }
    }

    private void updateTodaysHourlyForecastsMap() {
        var today = LocalDate.now();
        lastResponse.hourly.stream().filter(it -> it.date().equals(today)).forEach(it -> {
            var time = it.time();
            log.debug("Update hourly forcast at {} with {}", time, it.localDateTime());
            todaysHourlyForecastsMap.put(time, it);
        });
    }

    private Response downloadPastResponse(LocalDate date) throws IOException, InterruptedException {
        var nextDayMidnight = toTimestamp(date.plusDays(1).atStartOfDay(), apiZoneId());
        var url = baseUrl + "/timemachine" + query + "&dt=" + nextDayMidnight;
        return downloadResponse(url);
    }

    private Response downloadCurrentResponse() throws IOException, InterruptedException {
        var url = baseUrl + query;
        return downloadResponse(url);
    }

    private Response downloadResponse(String url) throws IOException, InterruptedException {
        log.debug("Download {}", url);
        try (var response = http.get(url)) {
            log.debug("Refreshing weather forecast");
            return mapper.readValue(response.body, Response.class);
        }
    }

    private ZoneId apiZoneId() {
        return lastResponse.zoneID();
    }

    private static record Response(Forecast current, List<DailyForecast> daily, List<Forecast> hourly,
            String timezone) {

        ZoneId zoneID() {
            return ZoneId.of(timezone);
        }

        @Data
        private static class Forecast {
            int clouds;
            int dt;
            double wind_speed;

            Instant instant() {
                return ofEpochSecond(dt);
            }

            LocalTime time() {
                return localDateTime().toLocalTime();
            }

            LocalDate date() {
                return localDateTime().toLocalDate();
            }

            Cloudiness cloudiness() {
                return new Cloudiness(clouds);
            }

            LocalDateTime localDateTime() {
                return instant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            }

            WindSpeed windSpeed() {
                return WindSpeed.fromMps(wind_speed);
            }
        }

        @Data
        private static class DailyForecast extends Forecast {
            private static record Temp(double max) {

            }

            Temp temp;
        }
    }

    @Override
    public WindSpeed windspeed() {
        return lastResponse.current.windSpeed();
    }

    @Override
    public Temperature highestDailyTemperature() throws ApiException, InterruptedException {
        LocalDate today = LocalDate.now();
        return lastResponse.daily.stream().filter(it -> it.date().equals(today)).findAny()
                .map(it -> Temperature.fromKelvin(it.temp.max)).orElseThrow(() -> new IllegalStateException(
                        String.format("Could not fetch highestDailyTemperature at %s", today)));
    }
}
