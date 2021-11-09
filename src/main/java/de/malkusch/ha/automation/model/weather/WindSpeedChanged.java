package de.malkusch.ha.automation.model.weather;

import de.malkusch.ha.shared.infrastructure.event.Event;

public record WindSpeedChanged(WindSpeed windSpeed) implements Event {

}
