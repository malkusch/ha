package de.malkusch.ha.automation.model.room;

public record RoomId(String id) {

    @Override
    public String toString() {
        return id;
    }
}
