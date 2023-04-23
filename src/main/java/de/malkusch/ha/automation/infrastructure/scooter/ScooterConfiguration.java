package de.malkusch.ha.automation.infrastructure.scooter;

import java.io.IOException;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.malkusch.ha.automation.infrastructure.socket.OfflineSocket;
import de.malkusch.ha.automation.infrastructure.socket.Socket;
import de.malkusch.ha.automation.infrastructure.socket.TuyaSocket;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.geo.Location;
import de.malkusch.ha.automation.model.scooter.Scooter;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.Api;
import de.malkusch.ha.shared.infrastructure.CoolDown;
import de.malkusch.niu.Niu;
import de.malkusch.tuya.TuyaApi;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class ScooterConfiguration {

    private final ScooterProperties scooterProperties;

    @ConfigurationProperties("electricity.scooter")
    @Component
    @Data
    public static class ScooterProperties {

        private Niu niu;

        @Data
        public static class Niu {
            private String account;
            private String password;
            private String countryCode;
        }

        private Wallbox wallbox;

        @Data
        public static class Wallbox {
            private Duration coolDown;
            private double balancingThreshold;

            private Socket tuyaSocket;

            @Data
            public static class Socket {
                private Duration timeout;
                private Duration expiration;
                private String deviceId;
                private String localKey;
            }
        }

        private ChargingRule chargingRule;

        @Data
        public static class ChargingRule {
            private Duration evaluationRate;
            private double maximumDistance;
            private MinimumCharge minimumCharge;
            private double maximumCharge;

            @Data
            public static class MinimumCharge {
                private double start;
                private double stop;
            }

            private ExcessCharging excessCharging;

            @Data
            public static class ExcessCharging {
                private Duration window;
                private double startExcess;
                private double stopExcess;
                private double startCharge;
            }
        }
    }

    @Bean
    Niu niu() throws IOException {
        var properties = this.scooterProperties.niu;
        return new Niu.Builder(properties.account, properties.password, properties.countryCode).build();
    }

    @Bean
    Scooter scooter() throws IOException {
        var niu = niu();
        var serialNumber = niu.vehicles()[0].serialNumber();
        var api = new NiuScooterApi(serialNumber, niu);
        return new Scooter(api);
    }

    @Bean
    TuyaApi.Factory tuyaSocketFactory() {
        var properties = scooterProperties.wallbox.tuyaSocket;
        var timeout = properties.timeout;
        var expiration = properties.expiration;

        return TuyaApi.buildFactory().withDeviceTimeout(timeout).withExpiration(expiration).factory();
    }

    @Bean
    Socket wallboxSocket() throws IOException {
        var properties = scooterProperties.wallbox.tuyaSocket;
        var deviceId = properties.deviceId;
        var localKey = properties.localKey;
        var factory = tuyaSocketFactory();

        var socket = new OfflineSocket(() -> new TuyaSocket(factory.api(deviceId, localKey)));
        return socket;
    }

    @Bean
    Api tuya() throws IOException {
        var socket = wallboxSocket();

        return new TuyaWallboxApi(socket);
    }

    private final Location location;

    @Bean
    ScooterWallbox scooterWallbox() throws IOException {
        var balancingThreshold = new Capacity(scooterProperties.wallbox.balancingThreshold);
        var coolDown = new CoolDown(scooterProperties.wallbox.coolDown);
        return new ScooterWallbox(location, tuya(), scooter(), coolDown, balancingThreshold);
    }
}
