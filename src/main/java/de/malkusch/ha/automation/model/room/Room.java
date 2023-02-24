package de.malkusch.ha.automation.model.room;

import de.malkusch.ha.automation.model.light.LightId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Room {

    public final RoomId id;
    public final LightId signal;

    public LightId signalLight() {
        return signal;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
