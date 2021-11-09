package de.malkusch.ha.automation.infrastructure.shutters;

import static de.malkusch.ha.automation.model.shutters.ShutterId.KUECHENTUER;
import static de.malkusch.ha.automation.model.shutters.ShutterId.TERRASSE;
import static de.malkusch.ha.automation.model.shutters.ShutterId.UTES_BUERO;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toUnmodifiableMap;

import java.time.Duration;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.shutters.Shutter;
import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class ShutterConfiguration {

    private final HttpClient http;
    private final ObjectMapper mapper;
    private final Properties properties;

    @ConfigurationProperties("shutters")
    @Component
    @Data
    public static class Properties {

        private Duration delay;
        private ShellyProperties shelly;

        @Data
        public static class ShellyProperties {
            private String url;
            private String key;
            private List<ShutterProperty> shutters;

            @Data
            public static class ShutterProperty {
                private ShutterId id;
                private String deviceId;
            }
        }
    }

    @Bean
    public Api loggingApi() {
        return new LoggingApi();
    }

    @Bean
    public Api shellyApi() {
        var deviceIds = properties.shelly.shutters.stream().collect(toUnmodifiableMap(it -> it.id, it -> it.deviceId));
        return new ShellyCloudApi(properties.shelly.url, properties.shelly.key, http, mapper, deviceIds);
    }

    @Bean
    public ShutterRepository shutters() {
        return new InMemoryShutterRepository(
                asList(shutter(KUECHENTUER), shutter(UTES_BUERO, shellyApi()), shutter(TERRASSE)));
    }

    private Shutter shutter(ShutterId id) {
        return shutter(id, loggingApi());
    }

    private Shutter shutter(ShutterId id, Api api) {
        return new Shutter(id, api, properties.delay);
    }
}
