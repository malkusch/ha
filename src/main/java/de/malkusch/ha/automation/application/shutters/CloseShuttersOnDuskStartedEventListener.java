package de.malkusch.ha.automation.application.shutters;

import static de.malkusch.ha.automation.model.shutters.ShutterId.TERRASSE;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.AstronomicalSunsetStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.CivilSunsetStarted;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.automation.model.weather.Weather;
import de.malkusch.ha.shared.model.ApiException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class CloseShuttersOnDuskStartedEventListener {

    @ConfigurationProperties("shutters.blinds.terrasse")
    @Component
    @Data
    public static class Properties {
        private double nightCloseThreshold;
    }

    private final ShutterRepository shutters;
    private final Properties properties;
    private final Weather weather;

    @EventListener
    public void onDuskStarted(CivilSunsetStarted event) throws ApiException, InterruptedException {
        log.info("Closing shutters at dusk");
        for (var shutter : shutters.findAll()) {
            if (shutter.id == TERRASSE) {
                continue;
            }
            shutter.close();
        }
    }

    @EventListener
    public void onNightStarted(AstronomicalSunsetStarted event) throws ApiException, InterruptedException {
        var threshold = new Temperature(properties.nightCloseThreshold);
        if (weather.temperature().isLessThan(threshold)) {
            log.info("Closing Terrasse blinds during a cold night");
            var blind = shutters.find(TERRASSE);
            blind.close();
        }
    }
}
