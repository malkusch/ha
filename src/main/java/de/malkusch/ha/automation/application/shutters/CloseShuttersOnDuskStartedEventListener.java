package de.malkusch.ha.automation.application.shutters;

import static de.malkusch.ha.automation.model.shutters.ShutterId.TERRASSE;
import static java.util.Arrays.asList;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.AstronomicalSunsetStarted;
import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.CivilSunsetStarted;
import de.malkusch.ha.automation.model.shutters.Shutter;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.automation.model.weather.Weather;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CloseShuttersOnDuskStartedEventListener {

    private final ShutterRepository shutters;
    private final Weather weather;
    private final Temperature coldNightTemperature;

    public CloseShuttersOnDuskStartedEventListener(
            @Value("${shutters.cold-night-temperature}") double coldNightTemperature, ShutterRepository shutters,
            Weather weather) {

        this.shutters = shutters;
        this.weather = weather;
        this.coldNightTemperature = new Temperature(coldNightTemperature);
    }

    @EventListener
    public void onDuskStarted(CivilSunsetStarted event) throws ApiException, InterruptedException {
        var allButTerrasse = shutters.findAll().stream().filter(it -> it.id != TERRASSE).toList();
        closeShuttersWhenColdNight(allButTerrasse);
    }

    @EventListener
    public void onNightStarted(AstronomicalSunsetStarted event) throws ApiException, InterruptedException {
        var onlyTerrasse = asList(shutters.find(TERRASSE));
        closeShuttersWhenColdNight(onlyTerrasse);
    }

    private void closeShuttersWhenColdNight(Collection<Shutter> shutters) throws ApiException, InterruptedException {
        var temperature = weather.temperature();
        if (!temperature.isLessThan(coldNightTemperature)) {
            log.info("Night too warm ({}) for closing shutters", temperature);
            return;
        }

        log.info("Closing shutters during a cold night ({})", temperature);
        for (var shutter : shutters) {
            shutter.close();
        }
    }
}
