package de.malkusch.ha.automation.application.shutters.hotDay;

import java.time.LocalTime;

import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.ha.shared.infrastructure.event.Event;

public record DirectSunLightEnded(LocalTime time, ShutterId shutter) implements Event {

}
