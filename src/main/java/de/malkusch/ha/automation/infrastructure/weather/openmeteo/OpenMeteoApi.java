package de.malkusch.ha.automation.infrastructure.weather.openmeteo;

import static java.time.temporal.ChronoUnit.HOURS;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.infrastructure.weather.openmeteo.OpenMeteoApi.Forecast.Daily;
import de.malkusch.ha.automation.infrastructure.weather.openmeteo.OpenMeteoApi.Forecast.Hourly;
import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.electricity.ElectricityPredictionService.SolarIrradianceForecast.Irradiance;
import de.malkusch.ha.automation.model.geo.Location;
import de.malkusch.ha.automation.model.weather.WindSpeed;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
final class OpenMeteoApi {

    private final HttpClient http;
    private final Location location;
    private final ObjectMapper mapper;
    private final TimeZone timeZone = TimeZone.getDefault();

    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    OpenMeteoApi(HttpClient http, Location location, ObjectMapper mapper) throws IOException, InterruptedException {

        this.http = http;
        this.location = location;
        this.mapper = mapper;
    }

    private static final int FORECAST_DAYS = 2;

    public Forecast fetchForecast(int pastDays) throws IOException, InterruptedException {
        var url = BASE_URL //
                + "?latitude=" + location.latitude() //
                + "&longitude=" + location.longitude() //
                + "&daily=temperature_2m_max,shortwave_radiation_sum" //
                + "&hourly=windspeed_10m,windgusts_10m" //
                + "&timezone=" + timeZone.getID() //
                + "&timeformat=iso8601" //
                + "&past_days=" + pastDays //
                + "&forecast_days=" + FORECAST_DAYS;

        log.debug("Download {}", url);
        ForecastResponse forcastResponse;
        try (var response = http.get(url)) {
            if (response.statusCode != 200) {
                throw new IOException("Fetching forcast resulted in status " + response.statusCode);
            }
            forcastResponse = mapper.readValue(response.body, ForecastResponse.class);
        }

        Map<LocalDate, Forecast.Daily> dailies = new HashMap<>();
        for (var i = 0; i < forcastResponse.daily.time.length; i++) {
            var date = LocalDate.parse(forcastResponse.daily.time[i]);
            var highestDailyTemperature = new Temperature(forcastResponse.daily.temperature_2m_max[i]);
            var globalIrradiance = new Irradiance(forcastResponse.daily.shortwave_radiation_sum[i]);
            var daily = new Forecast.Daily(highestDailyTemperature, globalIrradiance);
            dailies.put(date, daily);
        }

        Map<LocalDateTime, Forecast.Hourly> hourlies = new HashMap<>();
        for (var i = 0; i < forcastResponse.hourly.time.length; i++) {
            var time = Forecast.Hourly.canonical(LocalDateTime.parse(forcastResponse.hourly.time[i]));
            var windspeed = new WindSpeed(forcastResponse.hourly.windspeed_10m[i]);
            var windgusts = new WindSpeed(forcastResponse.hourly.windgusts_10m[i]);
            var hourly = new Forecast.Hourly(windspeed, windgusts);
            hourlies.put(time, hourly);
        }

        return new Forecast(dailies, hourlies);
    }

    private static record ForecastResponse(Daily daily, Hourly hourly, String timezone, double latitude,
            double longitude) {

        private static record Daily(String[] time, double[] temperature_2m_max, double[] shortwave_radiation_sum) {
        }

        private static record Hourly(String[] time, double[] windgusts_10m, double[] windspeed_10m) {
        }
    }

    static record Forecast(Map<LocalDate, Daily> daily, Map<LocalDateTime, Hourly> hourly) {
        static record Daily(Temperature highestDailyTemperature, Irradiance globalIrradiance) {
        }

        static record Hourly(WindSpeed windspeed, WindSpeed windgusts) {

            private static LocalDateTime canonical(LocalDateTime time) {
                return time.truncatedTo(HOURS);
            }

        }

        public Optional<Hourly> hourly(LocalDateTime time) {
            return Optional.ofNullable(hourly.get(Hourly.canonical(time)));
        }

        public Optional<Daily> daily(LocalDate date) {
            return Optional.ofNullable(daily.get(date));
        }
    }
}
