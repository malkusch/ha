package de.malkusch.ha;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import de.malkusch.ha.automation.infrastructure.shutters.ShellyCloudApi;
import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.shared.model.ApiException;
import de.malkusch.km200.KM200;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ExtendWith(MockitoExtension.class)
public class ApplicationIT {

    @MockBean
    KM200 km200;

    @TestConfiguration
    static class TestApplicationConfiguration {

        @Bean
        @Primary
        public ShellyCloudApi.Factory shellyCloudApiFactory() throws ApiException, InterruptedException {
            var api = mock(Api.class);
            when(api.state()).thenReturn(OPEN);
            return (id, deviceId) -> api;
        }
    }

    @Test
    void contextLoads() {
    }
}
