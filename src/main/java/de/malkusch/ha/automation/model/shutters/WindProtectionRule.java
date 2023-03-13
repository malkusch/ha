package de.malkusch.ha.automation.model.shutters;

import java.time.Duration;

import de.malkusch.ha.automation.model.Rule;
import de.malkusch.ha.automation.model.weather.Weather;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class WindProtectionRule implements Rule {

    private final Duration evaluationRate;
    private final ShutterRepository shutters;
    private final WindProtectionService<Shutter> shutterWindProtectionService;
    private final WindProtectionService<Blind> blindWindProtectionService;
    private final Weather weather;

    @Override
    public void evaluate() throws Exception {
        var windSpeed = weather.windspeed();
        for (var shutter : shutters.findAll()) {
            if (shutter instanceof Blind blind) {
                blindWindProtectionService.checkProtection(blind, windSpeed);
            } else {
                shutterWindProtectionService.checkProtection(shutter, windSpeed);
            }
        }
    }

    @Override
    public Duration evaluationRate() {
        return evaluationRate;
    }
}
