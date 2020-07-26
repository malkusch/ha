package de.malkusch.ha.shared.infrastructure.buderus;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;

@Configuration
class BuderusConfiguration {

    @ConfigurationProperties("buderus")
    @Component
    @Data
    public static class BuderusProperties {
        private String salt;
        private String gatewayPassword;
        private String privatePassword;
        private String host;
    }

    @Bean
    public BuderusApi buderusApi(BuderusProperties properties, ObjectMapper mapper) {
        return new BuderusApi(properties.host, properties.gatewayPassword, properties.privatePassword, properties.salt,
                mapper);
    }
}
