package de.malkusch.ha.automation.model.astronomy;

import java.time.LocalTime;
import java.time.ZonedDateTime;

import de.malkusch.ha.shared.infrastructure.event.Event;

public interface AstronomicalEvent extends Event {

    ZonedDateTime dateTime();
    
    default LocalTime time() {
        return dateTime().toLocalTime();
    }

    record AstronomicalSunriseStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record AstronomicalSunsetStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record CivilSunriseStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record CivilSunsetStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record NauticalSunriseStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record NauticalSunsetStarted(ZonedDateTime dateTime) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }
}
