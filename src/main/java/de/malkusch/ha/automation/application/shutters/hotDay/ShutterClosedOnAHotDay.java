package de.malkusch.ha.automation.application.shutters.hotDay;

import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.ha.shared.infrastructure.event.Event;

public record ShutterClosedOnAHotDay(ShutterId shutter) implements Event {


}
