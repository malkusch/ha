package de.malkusch.ha.automation.application.theater;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.room.RoomLightsRepository;
import de.malkusch.ha.automation.model.theater.AvrTurnedOff;
import de.malkusch.ha.automation.model.theater.AvrTurnedOn;
import de.malkusch.ha.automation.model.theater.Theater;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutomateRoomLightApplicationService {

    private final Theater theater;
    private final RoomLightsRepository roomLights;

    @EventListener
    public void start(AvrTurnedOn event) throws ApiException {
        log.info("Show starts: Turning off the lights");
        roomLights.find(theater.room).turnOff();
    }

    @EventListener
    public void end(AvrTurnedOff event) throws ApiException {
        log.info("Show ends: Turning on the lights");
        roomLights.find(theater.room).turnOn();
    }
}
