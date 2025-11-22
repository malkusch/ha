package de.malkusch.ha.automation.infrastructure.shutters;

import com.google.common.util.concurrent.RateLimiter;
import de.malkusch.ha.automation.infrastructure.shutters.ShutterConfiguration.Properties.ShellyProperties.ShutterProperty.DirectSunLightRangeProperty;
import de.malkusch.ha.automation.model.astronomy.Azimuth;
import de.malkusch.ha.automation.model.shutters.*;
import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.automation.model.weather.WindSpeed;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.JsonHttpExchange;
import de.malkusch.ha.shared.infrastructure.http.RateLimitingHttpClient;
import de.malkusch.ha.shared.model.ApiException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
            private List<ShutterProperty> blinds;

            @Data
            public static class ShutterProperty {
                private ShutterId id;
                private String deviceId;
                private DirectSunLightRangeProperty directSunLight;

                @Data
                public static class DirectSunLightRangeProperty {
                    private double start;
                    private double end;
                }
            }
        }

        private BlindsProperties blinds;

        @Data
        public static class BlindsProperties {
            private WindProtectionProperties windProtection;
        }
    }

    @Bean
    public WindProtectionService<Shutter> shutterWindProtectionService() {
        var properties = this.properties.windProtection;
        var releaseThreshold = new WindSpeed(properties.releaseWindSpeed);
        var protectThreshold = new WindSpeed(properties.protectWindSpeed);
        var protectionState = new Api.State(properties.protectionState);
        return new WindProtectionService<>(releaseThreshold, protectThreshold, protectionState,
                properties.lockDuration);
    }

    @Bean
    public WindProtectionService<Blind> blindWindProtectionService() {
        var properties = this.properties.blinds.windProtection;
        var releaseThreshold = new WindSpeed(properties.releaseWindSpeed);
        var protectThreshold = new WindSpeed(properties.protectWindSpeed);
        var protectionState = new Api.State(properties.protectionState);
        return new WindProtectionService<>(releaseThreshold, protectThreshold, protectionState,
                properties.lockDuration);
    }

    @Bean
    public ShutterRepository shutters() throws ApiException, InterruptedException {
        var shutters = new ArrayList<Shutter>();
        shutters.addAll(properties.shelly.shutters.stream()
                .map(it -> shellyShutter(it.id, it.deviceId, directSunLightRange(it.directSunLight)))
                .collect(toList()));
        shutters.addAll(properties.shelly.blinds.stream()
                .map(it -> shellyBlind(it.id, it.deviceId, directSunLightRange(it.directSunLight))).collect(toList()));

        return new InMemoryShutterRepository(shutters);
    }

    private static DirectSunLightRange directSunLightRange(DirectSunLightRangeProperty property) {
        if (property == null) {
            return DirectSunLightRange.EMPTY;
        }
        var start = new Azimuth(property.start);
        var end = new Azimuth(property.end);
        return new DirectSunLightRange(start, end);
    }

    private final HttpClient http;

    @Bean
    public JsonHttpExchange shellyJsonHttpExchange() {
        var limiter = RateLimiter.create(0.20);
        var shellyHttp = new RateLimitingHttpClient(http, limiter);
        return new JsonHttpExchange(shellyHttp, mapper);
    }

    @Bean
    public ShellyCloudV2Api.Factory shellyCloudApiFactory() {
        return (id, deviceId) -> new ShellyCloudV2Api(properties.shelly.url, properties.shelly.key, shellyJsonHttpExchange(), deviceId);
    }

    private Shutter shellyBlind(ShutterId id, String deviceId, DirectSunLightRange directSunLightRange) {
        try {
            return blind(id, shellyCloudApiFactory().build(id, deviceId), directSunLightRange);
        } catch (ApiException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private Shutter shellyShutter(ShutterId id, String deviceId, DirectSunLightRange directSunLightRange) {
        try {
            return shutter(id, shellyCloudApiFactory().build(id, deviceId), directSunLightRange);
        } catch (ApiException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private Shutter shutter(ShutterId id, Api api, DirectSunLightRange directSunLightRange)
            throws ApiException, InterruptedException {
        return new Shutter(id, api, properties.delay, directSunLightRange);
    }

    private Blind blind(ShutterId id, Api api, DirectSunLightRange directSunLightRange)
            throws ApiException, InterruptedException {
        return new Blind(id, api, properties.delay, directSunLightRange);
    }
}
