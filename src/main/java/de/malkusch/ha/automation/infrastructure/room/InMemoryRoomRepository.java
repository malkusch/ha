package de.malkusch.ha.automation.infrastructure.room;

import java.util.Collection;
import java.util.List;

import de.malkusch.ha.automation.model.room.Room;
import de.malkusch.ha.automation.model.room.RoomRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class InMemoryRoomRepository implements RoomRepository {

    private final List<Room> rooms;

    @Override
    public Collection<Room> findAll() {
        return rooms;
    }

}
