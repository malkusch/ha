package de.malkusch.ha.automation.infrastructure.prometheus;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.Data;

@Configuration
class PrometheusConfiguration {

    @ConfigurationProperties("prometheus")
    @Component
    @Data
    public static class PrometheusProperties {
        private String url;
        private Duration delay;
    }

    @Bean
    public Electricity electricity(HttpClient http, ObjectMapper mapper, PrometheusProperties properties) {
        return new PrometheusElectricity(http, mapper, properties.url, properties.delay);
    }
}
