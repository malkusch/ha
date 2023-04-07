package de.malkusch.ha.automation.infrastructure.prometheus;

import java.util.HashMap;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.infrastructure.electricity.ElectricityConfiguration.ElectricityProperties;
import de.malkusch.ha.automation.model.climate.ClimateService;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class PrometheusConfiguration {

    private final PrometheusProperties properties;

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

    private final HttpClient http;
    private final ObjectMapper mapper;

    @Bean
    Prometheus prometheus() {
        return new PrometheusHttpClient(http, mapper, properties.url);
    }

    private final ElectricityProperties electricityProperties;

    @Bean
    Electricity electricity() {
        var fullyCharged = new Capacity(electricityProperties.getBattery().getFullyCharged());
        return new PrometheusElectricity(prometheus(), fullyCharged);
    }

    @Bean
    ClimateService climateService() {
        var outsidePrefix = properties.climate.outsidePrefix;
        var map = new HashMap<RoomId, String>();
        for (var property : properties.climate.rooms) {
            var room = new RoomId(property.room);
            var prefix = property.prefix;
            map.put(room, prefix);
        }
        return new PrometheusClimateService(prometheus(), outsidePrefix, map);
    }
}
