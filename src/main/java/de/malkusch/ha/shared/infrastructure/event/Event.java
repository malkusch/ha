package de.malkusch.ha.shared.infrastructure.event;

public interface Event {

    static enum NullEvent implements Event {
        NULL_EVENT;
    }

}
