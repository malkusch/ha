package de.malkusch.ha.automation.infrastructure.dehumidifier;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.infrastructure.MapRepository;
import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import de.malkusch.ha.shared.infrastructure.http.HttpClient.Field;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
class DehumidifierConfiguration {

    @ConfigurationProperties("dehumidifier.midea")
    @Component
    @Data
    public class ApiProperties {
        private String appKey;
        private String loginAccount;
        private String password;
        private Map<String, String> requestParameters;
    }

    @Bean
    public MideaApi mideaApi(ApiProperties properties, HttpClient http, ObjectMapper mapper) {
        var requestParameters = properties.requestParameters.entrySet().stream()
                .map(it -> new Field(it.getKey(), it.getValue())).toArray(Field[]::new);
        return new MideaApi(properties.appKey, properties.loginAccount, properties.password, requestParameters, http,
                mapper);
    }

    @Bean
    public DehumidifierRepository dehumidifiers(MideaApi api) throws ApiException, InterruptedException {
        var dehumidifiers = api.detect().collect(toUnmodifiableMap(it -> it.id, it -> it));
        dehumidifiers.values().forEach(it -> log.info("Found dehumidifier {}", it));
        var repository = new MapRepository<>(dehumidifiers);
        return repository::find;
    }
}
