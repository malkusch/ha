package de.malkusch.ha.automation.application.shutters;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.automation.model.weather.WindSpeedChanged;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WindSpeedChangedEventHandler {

    private final ShutterRepository shutters;

    @EventListener
    public void onWindSpeedChanged(WindSpeedChanged event) throws ApiException, InterruptedException {
        for (var shutter : shutters.findAll()) {
            shutter.onWindSpeedChanged(event);
        }
    }
}
