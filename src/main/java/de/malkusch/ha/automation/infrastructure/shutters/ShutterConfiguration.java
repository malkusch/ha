package de.malkusch.ha.automation.infrastructure.shutters;

import static de.malkusch.ha.automation.model.shutters.ShutterId.TERRASSE;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;

import de.malkusch.ha.automation.model.shutters.Shutter;
import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.automation.model.shutters.WindProtectionService;
import de.malkusch.ha.automation.model.weather.WindSpeed;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.RateLimitingHttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class ShutterConfiguration {

    private final ObjectMapper mapper;
    private final Properties properties;

    @ConfigurationProperties("shutters")
    @Component
    @Data
    public static class Properties {

        private Duration delay;
        private WindProtectionProperties windProtection;

        @Data
        public static class WindProtectionProperties {
            private int releaseWindSpeed;
            private int protectWindSpeed;
            private int protectionState;
            private Duration lockDuration;
        }

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
    public WindProtectionService windProtectionService() {
        var releaseThreshold = new WindSpeed(properties.windProtection.releaseWindSpeed);
        var protectThreshold = new WindSpeed(properties.windProtection.protectWindSpeed);
        var protectionState = new Api.State(properties.windProtection.protectionState);
        return new WindProtectionService(releaseThreshold, protectThreshold, protectionState,
                properties.windProtection.lockDuration);
    }

    @Bean
    public ShutterRepository shutters() throws ApiException, InterruptedException {
        var shutters = new ArrayList<Shutter>();
        shutters.addAll(
                properties.shelly.shutters.stream().map(it -> shellyShutter(it.id, it.deviceId)).collect(toList()));
        shutters.addAll(asList(shutter(TERRASSE)));

        return new InMemoryShutterRepository(shutters);
    }

    private final HttpClient http;

    @Bean
    public HttpClient shellyHttpClient() {
        var limiter = RateLimiter.create(0.25);
        return new RateLimitingHttpClient(http, limiter);
    }

    private Shutter shellyShutter(ShutterId id, String deviceId) {
        try {
            return shutter(id, new ShellyCloudApi(properties.shelly.url, properties.shelly.key, shellyHttpClient(),
                    mapper, deviceId));
        } catch (ApiException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private Shutter shutter(ShutterId id) throws ApiException, InterruptedException {
        return shutter(id, new LoggingApi(id));
    }

    private Shutter shutter(ShutterId id, Api api) throws ApiException, InterruptedException {
        return new Shutter(id, api, properties.delay);
    }
}
