package de.malkusch.ha.automation.infrastructure.prometheus;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.Data;

@Configuration
class PrometheusConfiguration {

    @ConfigurationProperties("prometheus")
    @Component
    @Data
    public static class PrometheusProperties {
        private String url;
    }

    @Bean
    public Prometheus prometheus(HttpClient http, ObjectMapper mapper, PrometheusProperties properties) {
        return new Prometheus(http, mapper, properties.url);
    }
}
