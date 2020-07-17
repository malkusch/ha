package de.malkusch.ha.automation.infrastructure.dehumidifier;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.automation.infrastructure.MapRepository;
import de.malkusch.ha.automation.infrastructure.dehumidifier.midea.MideaApi;
import de.malkusch.ha.automation.infrastructure.dehumidifier.midea_python.PythonMideaApi;
import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.NotFoundException;
import de.malkusch.ha.automation.model.Watt;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierId;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
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
    public static class ApiProperties {
        private String appKey;
        private int power;
        private String path;
        private String loginAccount;
        private String password;
        private Map<String, String> requestParameters;
    }

    @Bean
    public DehumidifierRepository dehumidifiers(ApiProperties properties, HttpClient http, ObjectMapper mapper)
            throws ApiException, InterruptedException, IOException {

        var pythonApi = new PythonMideaApi(properties.loginAccount, properties.password, properties.path);

        var requestParameters = properties.requestParameters.entrySet().stream()
                .map(it -> new Field(it.getKey(), it.getValue())).toArray(Field[]::new);
        var restApi = new MideaApi(properties.appKey, properties.loginAccount, properties.password, requestParameters,
                http, mapper);

        var power = new Watt(properties.power);
        var dehumidifiers = restApi.detect(power).map(it -> new Dehumidifier(it.id, it.power, pythonApi))
                .collect(toUnmodifiableMap(it -> it.id, it -> it));
        dehumidifiers.values().forEach(it -> log.info("Found dehumidifier {}", it));
        var repository = new MapRepository<>(dehumidifiers);
        return new DehumidifierRepository() {
            public Dehumidifier find(DehumidifierId id) throws NotFoundException {
                return repository.find(id);
            }

            public Collection<Dehumidifier> findAll() {
                return repository.findAll();
            }
        };
    }
}
