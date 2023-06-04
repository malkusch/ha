package de.malkusch.ha.automation.application.heater;

import java.util.concurrent.Callable;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.heater.TemporaryTemperatureService;
import de.malkusch.ha.automation.model.heater.Heater;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class HeaterApplicationService {

    private final TemporaryTemperatureService temporaryTemperatureService;
    private final Heater heater;

    public HeaterState heaterState() {
        var resetTemperature = query(temporaryTemperatureService::resetTemperature);
        var changed = temporaryTemperatureService.isChanged();
        var temperature = query(heater::heaterTemperature);

        return new HeaterState(resetTemperature, temperature, changed);
    }

    public static record HeaterState(String resetTemperature, String temperature, boolean changed) {
    }

    private static String query(Callable<Object> query) {
        try {
            return query.call().toString();
        } catch (Exception e) {
            return String.format("%s: %s", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
