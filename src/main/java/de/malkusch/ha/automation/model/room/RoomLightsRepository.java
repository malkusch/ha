package de.malkusch.ha.automation.model.room;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.light.LightRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public final class RoomLightsRepository {

    private final RoomRepository rooms;
    private final LightRepository lights;

    public RoomLights find(RoomId roomId) {
        var room = rooms.find(roomId);
        var roomLights = room.roomLights.stream().map(lights::find).toList();
        return new RoomLights(roomLights);
    }

}
