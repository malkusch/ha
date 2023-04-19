package de.malkusch.ha.automation.model.astronomy;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import de.malkusch.ha.shared.infrastructure.event.Event;

public interface AstronomicalEvent extends Event {

    ZonedDateTime dateTime();
    
    default LocalTime time() {
        return dateTime().toLocalTime();
    }

    public record AstronomicalSunriseStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public record AstronomicalSunsetStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public record CivilSunriseStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public record CivilSunsetStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public record NauticalSunriseStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public record NauticalSunsetStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }
}
