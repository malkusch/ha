package de.malkusch.ha.automation.infrastructure.weather.openmeteo;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.weather.openmeteo.OpenMeteoApi.Forecast.Daily;
import de.malkusch.ha.automation.model.electricity.ElectricityPredictionService.SolarIrradianceForecast;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
final class OpenMeteoSolarIrradianceForecast implements SolarIrradianceForecast {

    private final ForecastFactory forecastFactory;

    @Override
    public Irradiance globalIrradiance(LocalDate date) {
        var forecast = forecastFactory.forecast();
        return forecast.daily(date) //
                .map(Daily::globalIrradiance) //
                .orElseThrow();
    }

}
