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
import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.NotFoundException;
import de.malkusch.ha.automation.model.Watt;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierId;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.Data;

@Configuration
class DehumidifierConfiguration {

    @ConfigurationProperties("dehumidifier.tasmota")
    @Component
    @Data
    public static class TasmotaProperties {

        private List<Device> devices;

        @Data
        public static class Device {
            private String name;
            private String url;
            private int power;
        }
    }

    @Bean
    public DehumidifierRepository dehumidifiers(TasmotaProperties properties, HttpClient http, ObjectMapper mapper)
            throws ApiException, InterruptedException, IOException {

        var map = new HashMap<DehumidifierId, Dehumidifier>();
        for (var device : properties.devices) {
            var api = new TasmotaDehumidiferApi(new TasmotaApi(device.url, http, mapper));
            var id = new DehumidifierId(device.name);
            var power = new Watt(device.power);
            var dehumidifier = new Dehumidifier(id, power, api);
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
