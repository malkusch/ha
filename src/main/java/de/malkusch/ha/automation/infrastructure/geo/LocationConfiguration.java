package de.malkusch.ha.automation.infrastructure.geo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.model.geo.Location;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class LocationConfiguration {

    private final LocationProperties properties;

    @ConfigurationProperties("location")
    @Component
    @Data
    public static class LocationProperties {
        public double latitude;
        public double longitude;
    }

    @Bean
    Location location() {
        return new Location(properties.latitude, properties.longitude);
    }
}
