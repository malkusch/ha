package de.malkusch.ha.automation.infrastructure.scooter;

import java.io.IOException;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import de.malkusch.ha.automation.infrastructure.socket.OfflineSocket;
import de.malkusch.ha.automation.infrastructure.socket.Socket;
import de.malkusch.ha.automation.infrastructure.socket.TuyaSocketFactory;
import de.malkusch.ha.automation.model.electricity.Capacity;
import de.malkusch.ha.automation.model.electricity.Electricity;
import de.malkusch.ha.automation.model.electricity.Watt;
import de.malkusch.ha.automation.model.scooter.Scooter;
import de.malkusch.ha.automation.model.scooter.ScooterChargingRule;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox;
import de.malkusch.ha.automation.model.scooter.ScooterWallbox.Api;
import de.malkusch.niu.Niu;
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
        var api = niu();
        var serialNumber = api.vehicles()[0].serialNumber();
        return new NiuScooter(serialNumber, api);
    }

    private final Gson gson;

    @Bean
    TuyaSocketFactory tuyaSocketFactory() {
        var properties = scooterProperties.wallbox.tuyaSocket;
        var timeout = properties.timeout;
        var expiration = properties.expiration;

        return new TuyaSocketFactory(gson, timeout, expiration);
    }

    @Bean
    Socket wallboxSocket() throws IOException {
        var properties = scooterProperties.wallbox.tuyaSocket;
        var deviceId = properties.deviceId;
        var localKey = properties.localKey;
        var factory = tuyaSocketFactory();

        var socket = new OfflineSocket(() -> factory.build(deviceId, localKey));
        return socket;
    }

    @Bean
    Api tuya() throws IOException {
        var socket = wallboxSocket();

        return new TuyaWallboxApi(socket);
    }

    @Bean
    ScooterWallbox scooterWallbox() throws IOException {
        var balancingThreshold = new Capacity(scooterProperties.wallbox.balancingThreshold);
        return new ScooterWallbox(tuya(), scooter(), scooterProperties.wallbox.coolDown, balancingThreshold);
    }

    @Bean
    ScooterChargingRule scooterChargingRule(Electricity electricity) throws IOException {
        var properties = scooterProperties.chargingRule;
        var evaluationRate = properties.evaluationRate;
        var minimumStartCharge = new Capacity(properties.minimumCharge.start);
        var minimumStopCharge = new Capacity(properties.minimumCharge.stop);
        var maximumCharge = new Capacity(properties.maximumCharge);

        var excessWindow = properties.excessCharging.window;
        var startExcess = new Watt(properties.excessCharging.startExcess);
        var stopExcess = new Watt(properties.excessCharging.stopExcess);
        var excessStartCharge = new Capacity(properties.excessCharging.startCharge);

        return new ScooterChargingRule(evaluationRate, minimumStartCharge, minimumStopCharge, maximumCharge, scooter(),
                scooterWallbox(), electricity, excessWindow, startExcess, stopExcess, excessStartCharge);
    }
}
