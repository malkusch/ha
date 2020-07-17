package de.malkusch.ha.automation.infrastructure.prometheus;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.model.Electricity;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;

@Configuration
class PrometheusConfiguration {

    @Bean
    public Electricity electricity(HttpClient http, ObjectMapper mapper, @Value("${prometheus.host}") String host) {
        return new PrometheusElectricity(http, mapper, host);
    }
}
