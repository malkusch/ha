package de.malkusch.ha.automation.model.heater;

import static java.time.LocalDate.now;

import java.time.Duration;

import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.weather.Cloudiness;
import de.malkusch.ha.automation.model.weather.Weather;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class ReduceHotWaterInWinterRule implements Rule {

    private final Weather weather;
    private final Cloudiness threshold;
    private final Heater heater;
    private final TemporayHotWaterTemperatureService hotWaterTemperatureService;
    private final Temperature delta;
    private final Duration evaluationRate;

    @Override
    public void evaluate() throws ApiException, InterruptedException {
        if (!heater.isWinter()) {
            hotWaterTemperatureService.reset();
            return;
        }

        if (weather.cloudiness(now()).isGreaterThan(threshold)) {
            log.info("Reducing hot water because of cloudiness");
            hotWaterTemperatureService.reduceBy(delta);

        } else {
            hotWaterTemperatureService.reset();
        }
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
