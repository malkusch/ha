package de.malkusch.ha.automation.infrastructure.dehumidifier;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.infrastructure.MapRepository;
import de.malkusch.ha.automation.infrastructure.TasmotaApi;
import de.malkusch.ha.automation.model.NotFoundException;
import de.malkusch.ha.automation.model.Percent;
import de.malkusch.ha.automation.model.climate.Humidity;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierId;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.automation.model.dehumidifier.DesiredHumidity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.automation.model.room.RoomId;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
class DehumidifierConfiguration {

    @ConfigurationProperties("dehumidifier")
    @Component
    @Data
    public static class TasmotaProperties {

        private List<Device> tasmota;

        @Data
        public static class Device {
            private String name;
            private String room;
            private String url;
            private int power;
            private Humidity desiredHumidity;

            @Data
            public static class Humidity {
                private double minimum;
                private double maximum;
            }
        }
    }

    @Bean
    public DehumidifierRepository dehumidifiers(TasmotaProperties properties, HttpClient http, ObjectMapper mapper)
            throws ApiException, InterruptedException, IOException {

        var map = new HashMap<DehumidifierId, Dehumidifier>();
        for (var device : properties.tasmota) {
            var api = new TasmotaDehumidiferApi(new TasmotaApi(device.url, http, mapper));
            var room = new RoomId(device.room);
            var id = new DehumidifierId(device.name);
            var power = new Watt(device.power);
            var minimumHumidity = new Humidity(new Percent(device.desiredHumidity.minimum));
            var maximumHumidity = new Humidity(new Percent(device.desiredHumidity.maximum));
            var desiredHumidity = new DesiredHumidity(minimumHumidity, maximumHumidity);
            var dehumidifier = new Dehumidifier(id, room, power, desiredHumidity, api);

            log.info("Configured dehumidifier {} in {} with desired humidity {}", id, room, desiredHumidity);

            map.put(id, dehumidifier);
        }
        var repository = new MapRepository<>(map);
        return new DehumidifierRepository() {
            public Dehumidifier find(DehumidifierId id) throws NotFoundException {
                return repository.find(id);
            }

            public Collection<Dehumidifier> findAll() {
                return repository.findAll();
            }
        };
    }
}
