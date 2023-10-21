package de.malkusch.ha.automation.model.room;

import java.util.Collection;

import de.malkusch.ha.automation.model.light.LightId;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Room {

    public final RoomId id;
    final Collection<LightId> roomLights;

    @Override
    public String toString() {
        return id.toString();
    }
}
