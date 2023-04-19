package de.malkusch.ha.automation.model.astronomy;

import java.time.LocalTime;

import de.malkusch.ha.shared.infrastructure.event.Event;

public interface AstronomicalEvent extends Event {

    LocalTime time();

    record AstronomicalSunriseStarted(LocalTime time) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record AstronomicalSunsetStarted(LocalTime time) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record CivilSunriseStarted(LocalTime time) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record CivilSunsetStarted(LocalTime time) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record NauticalSunriseStarted(LocalTime time) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    record NauticalSunsetStarted(LocalTime time) implements AstronomicalEvent {

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }
}
