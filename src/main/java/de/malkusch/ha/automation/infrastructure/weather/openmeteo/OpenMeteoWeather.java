package de.malkusch.ha.automation.infrastructure.weather.openmeteo;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.weather.openmeteo.OpenMeteoApi.Forecast.Daily;
import de.malkusch.ha.automation.infrastructure.weather.openmeteo.OpenMeteoApi.Forecast.Hourly;
import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.weather.Weather;
import de.malkusch.ha.automation.model.weather.WindSpeed;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
final class OpenMeteoWeather implements Weather {

    private final ForecastFactory forecastFactory;

    @Override
    public Temperature highestDailyTemperature() throws ApiException, InterruptedException {
        var forecast = forecastFactory.forecast();
        return forecast.daily(LocalDate.now()) //
                .map(Daily::highestDailyTemperature) //
                .orElseThrow();
    }

    @Override
    public WindSpeed windspeed() throws ApiException, InterruptedException {
        var forecast = forecastFactory.forecast();
        return forecast.hourly(LocalDateTime.now()) //
                .map(Hourly::windspeed) //
                .orElseThrow();
    }

    @Override
    public Instant lastUpdate() {
        return forecastFactory.forecast().timestamp();
    }
}
