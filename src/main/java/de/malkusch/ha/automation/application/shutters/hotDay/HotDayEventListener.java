package de.malkusch.ha.automation.application.shutters.hotDay;

import static de.malkusch.ha.shared.infrastructure.event.EventPublisher.publish;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.Temperature;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.automation.model.weather.Weather;
import de.malkusch.ha.shared.model.ApiException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HotDayEventListener {

    public HotDayEventListener(ShutterRepository shutters, Weather weather,
            @Value("${shutters.minimum-highest-daily-temperature}") double minimumHighestDailyTemperature) {
        this.shutters = shutters;
        this.weather = weather;
        this.minimumHighestDailyTemperature = new Temperature(minimumHighestDailyTemperature);
    }

    private final ShutterRepository shutters;
    private final Weather weather;
    private final Temperature minimumHighestDailyTemperature;

    @EventListener
    public void close(DirectSunLightStarted event) throws ApiException, InterruptedException {
        var highestDailyTemperature = weather.highestDailyTemperature();
        if (highestDailyTemperature.isLessThan(minimumHighestDailyTemperature)) {
            log.debug("{} is not warm enough to close the shutter {}", highestDailyTemperature, event.shutter());
            return;
        }

        var shutter = shutters.find(event.shutter());
        log.info("Closing {} on a hot day ({})", shutter, highestDailyTemperature);
        shutter.close();
        publish(new ShutterClosedOnAHotDay(shutter.id));
    }

    @EventListener
    public void open(DirectSunLightEnded event) throws ApiException, InterruptedException {
        var shutter = shutters.find(event.shutter());
        log.info("Opening {} because direct sun light ended", shutter);
        shutter.open();
    }
}
