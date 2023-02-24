package de.malkusch.ha.automation.infrastructure.room;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.model.climate.CO2;
import de.malkusch.ha.automation.model.climate.ClimateService;
import de.malkusch.ha.automation.model.climate.Dust;
import de.malkusch.ha.automation.model.light.LightId;
import de.malkusch.ha.automation.model.room.OpenWindowRule;
import de.malkusch.ha.automation.model.room.OpenWindowRule.CO2Threshold;
import de.malkusch.ha.automation.model.room.Room;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.automation.model.room.RoomRepository;
import de.malkusch.ha.automation.model.room.SignalService;
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
            private String signalLight;
        }

        private Rules rules;

        @Data
        public static class Rules {

            private OpenWindow openWindow;

            @Data
            public static class OpenWindow {
                private Duration evaluationRate;
                private int dustBufferPm25;

                private CO2ThresholdProperties co2Threshold;

                @Data
                public static class CO2ThresholdProperties {
                    private int best;
                    private int old;
                    private int unhealthy;
                }
            }
        }
    }

    @Bean
    public OpenWindowRule openWindowRule(RoomProperties properties, ClimateService climateService, RoomRepository rooms,
            SignalService signalService) {

        var openWindowPropeties = properties.rules.openWindow;
        var cO2ThresholdProperties = openWindowPropeties.co2Threshold;
        CO2Threshold co2Threshold = new CO2Threshold(new CO2(cO2ThresholdProperties.best),
                new CO2(cO2ThresholdProperties.old), new CO2(cO2ThresholdProperties.unhealthy));

        Dust.PM2_5 buffer = new Dust.PM2_5(openWindowPropeties.dustBufferPm25);
        return new OpenWindowRule(signalService, climateService, rooms, openWindowPropeties.evaluationRate,
                co2Threshold, buffer);
    }

    @Bean
    public RoomRepository rooms(RoomProperties properties) {
        var rooms = new ArrayList<Room>();
        for (var property : properties.rooms) {
            var id = new RoomId(property.id);
            var signal = new LightId(property.signalLight);
            var room = new Room(id, signal);
            rooms.add(room);
        }
        return new InMemoryRoomRepository(rooms);
    }
}
