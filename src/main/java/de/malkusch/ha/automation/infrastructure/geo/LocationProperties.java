package de.malkusch.ha.automation.infrastructure.geo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@ConfigurationProperties("location")
@Component
@Data
public class LocationProperties {
    public String latitude;
    public String longitude;
}
