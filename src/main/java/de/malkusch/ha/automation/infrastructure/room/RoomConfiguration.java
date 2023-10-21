package de.malkusch.ha.automation.infrastructure.room;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.model.light.LightId;
import de.malkusch.ha.automation.model.room.Room;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.automation.model.room.RoomRepository;
import lombok.Data;

@Configuration
class RoomConfiguration {

    @ConfigurationProperties("room")
    @Component
    @Data
    public static class RoomProperties {

        private List<RoomProperty> rooms;

        @Data
        public static class RoomProperty {
            private String id;
            private Collection<String> lights;
        }
    }

    @Bean
    public RoomRepository rooms(RoomProperties properties) {
        var rooms = new ArrayList<Room>();
        for (var property : properties.rooms) {
            var id = new RoomId(property.id);
            var roomLights = property.lights.stream() //
                    .map(LightId::new)//
                    .toList();
            var room = new Room(id, roomLights);
            rooms.add(room);
        }
        return new InMemoryRoomRepository(rooms);
    }
}
