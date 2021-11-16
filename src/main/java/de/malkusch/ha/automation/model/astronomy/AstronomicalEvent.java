package de.malkusch.ha.automation.model.astronomy;

import java.time.LocalTime;

import de.malkusch.ha.shared.infrastructure.event.Event;

public interface AstronomicalEvent extends Event {

    LocalTime time();

    record AstronomicalSunriseStarted(LocalTime time) implements AstronomicalEvent {
    }

    record AstronomicalSunsetStarted(LocalTime time) implements AstronomicalEvent {
    }

    record CivilSunriseStarted(LocalTime time) implements AstronomicalEvent {
    }

    record CivilSunsetStarted(LocalTime time) implements AstronomicalEvent {
    }

    record NauticalSunriseStarted(LocalTime time) implements AstronomicalEvent {
    }

    record NauticalSunsetStarted(LocalTime time) implements AstronomicalEvent {
    }

}
