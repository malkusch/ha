package de.malkusch.ha.automation.infrastructure.prometheus;

import java.util.HashMap;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.climate.ClimateService;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.Data;

@Configuration
class PrometheusConfiguration {

    @ConfigurationProperties("prometheus")
    @Component
    @Data
    public static class PrometheusProperties {
        private String url;

        private Climate climate;

        @Data
        public static class Climate {
            private String outsidePrefix;
            private List<Room> rooms;

            @Data
            public static class Room {
                private String prefix;
                private String room;
            }
        }
    }

    @Bean
    public Prometheus prometheus(HttpClient http, ObjectMapper mapper, PrometheusProperties properties) {
        return new Prometheus(http, mapper, properties.url);
    }

    @Bean
    public ClimateService climateService(Prometheus prometheus, PrometheusProperties properties) {
        var outsidePrefix = properties.climate.outsidePrefix;
        var map = new HashMap<RoomId, String>();
        for (var property : properties.climate.rooms) {
            var room = new RoomId(property.room);
            var prefix = property.prefix;
            map.put(room, prefix);
        }
        return new PrometheusClimateService(prometheus, outsidePrefix, map);
    }
}
