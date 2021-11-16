package de.malkusch.ha.automation.application.shutters;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.astronomy.AstronomicalEvent.CivilSunriseStarted;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class OpenAllShuttersOnDawnStartedEventListener {

    private final ShutterRepository shutters;

    @EventListener
    public void onDawnStarted(CivilSunriseStarted event) throws ApiException, InterruptedException {
        log.info("Opening all shutters at dawn");
        for (var shutter : shutters.findAll()) {
            shutter.open();
        }
    }
}
