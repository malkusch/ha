package de.malkusch.ha;

import static de.malkusch.ha.automation.model.shutters.Shutter.Api.State.OPEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import de.malkusch.ha.automation.infrastructure.prometheus.Prometheus;
import de.malkusch.ha.automation.infrastructure.shutters.ShellyCloudApi;
import de.malkusch.ha.automation.model.shutters.Shutter.Api;
import de.malkusch.ha.shared.model.ApiException;
import de.malkusch.km200.KM200;
import de.malkusch.km200.KM200Exception;
import de.malkusch.niu.Niu;
import de.malkusch.niu.Niu.BatteryInfo;
import de.malkusch.niu.Niu.Vehicle;

@SpringBootTest
@ActiveProfiles(profiles = "test")
public class ApplicationIT {

    @TestConfiguration
    static class TestApplicationConfiguration {

        @Bean
        @Primary
        public ShellyCloudApi.Factory shellyCloudApiFactory() throws ApiException, InterruptedException {
            var api = mock(Api.class);
            when(api.state()).thenReturn(OPEN);
            return (id, deviceId) -> api;
        }

        @Bean
        @Primary
        public Prometheus prometheus() throws ApiException, InterruptedException {
            var api = mock(Prometheus.class);
            when(api.query(any(), any(Instant.class))).thenReturn(new BigDecimal(1));
            when(api.query(any(), any(LocalDate.class))).thenReturn(new BigDecimal(1));
            when(api.query(any())).thenReturn(new BigDecimal(1));
            return api;
        }

        @Bean
        @Primary
        public KM200 km200() throws KM200Exception, IOException, InterruptedException {
            var km200 = mock(KM200.class);
            when(km200.queryBigDecimal(any())).thenReturn(new BigDecimal(1));
            return km200;
        }

        @Bean
        @Primary
        public Niu niu() throws IOException {
            var niu = mock(Niu.class);

            when(niu.batteryInfo(any())).thenReturn(new BatteryInfo(false, 66, 23, 55, 1));

            var vehicleInfo = new Niu.VehicleInfo(null, null, 0, 0, 0, 0, null, 0, null, 0, 0, true, 0, 1, null);
            when(niu.vehicle(any())).thenReturn(vehicleInfo);

            when(niu.vehicles()).thenReturn(new Vehicle[] { new Vehicle("asdfdsf", "foo") });
            return niu;
        }
    }

    @Test
    void contextLoads() {
    }
}
