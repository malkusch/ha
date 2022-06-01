package de.malkusch.ha.automation.model.room;

import java.util.Collection;

public interface RoomRepository {

    Collection<Room> findAll();

}
