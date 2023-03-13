package de.malkusch.ha.automation.infrastructure.weather.openmeteo;

import java.io.IOException;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.weather.openmeteo.OpenMeteoApi.Forecast;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
class ForecastFactory {

    private final OpenMeteoApi api;
    private volatile Forecast forecast;

    ForecastFactory(OpenMeteoApi api) throws IOException, InterruptedException {
        this.api = api;
        forecast = api.fetchForecast(pastDays);
    }

    public Forecast forecast() {
        return forecast;
    }

    private volatile int pastDays = 15;

    @Scheduled(cron = "${open-meteo.forecast.refresh-cron}")
    void refreshForecast() throws IOException, InterruptedException {
        log.debug("Refreshing forecast");
        forecast = api.fetchForecast(pastDays);
    }
}
