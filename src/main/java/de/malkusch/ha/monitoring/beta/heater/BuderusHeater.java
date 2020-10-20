package de.malkusch.ha.monitoring.beta.heater;

import static java.lang.System.exit;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.malkusch.ha.shared.infrastructure.buderus.BuderusApi;
import io.prometheus.client.Gauge;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BuderusHeater {

    private final BuderusApi api;

    private final ScheduledExecutorService scheduler = newSingleThreadScheduledExecutor(r -> {
        var thread = new Thread(r, "BuderusHeater");
        thread.setUncaughtExceptionHandler((t, e) -> {
            log.error("Shutting down due to an error in BuderusHeater", e);
            exit(-1);
        });
        thread.setDaemon(true);
        return thread;
    });

    BuderusHeater(BuderusApi api, @Value("${buderus.queryRate}") Duration rate) throws Exception {
        this.api = api;

        scheduleUpdate("/dhwCircuits/dhw1/actualTemp", rate);
        scheduleUpdate("/dhwCircuits/dhw1/waterFlow", rate);

        scheduleUpdate("/heatingCircuits/hc1/actualSupplyTemperature", rate);
        scheduleUpdate("/heatingCircuits/hc1/currentRoomSetpoint", rate);
        scheduleUpdate("/heatingCircuits/hc1/pumpModulation", rate);

        scheduleUpdate("/heatSources/actualModulation", rate);
        scheduleUpdate("/heatSources/actualSupplyTemperature", rate);
        scheduleUpdate("/heatSources/applianceSupplyTemperature", rate);
        scheduleUpdate("/heatSources/CHpumpModulation", rate);
        scheduleUpdate("/heatSources/energyMonitoring/consumption", rate);
        scheduleUpdate("/heatSources/fanSpeed_setpoint", rate);
        scheduleUpdate("/heatSources/hs1/actualModulation", rate);
        scheduleUpdate("/heatSources/nominalCHPower", rate);
        scheduleUpdate("/heatSources/nominalDHWPower", rate);
        // scheduleUpdate("/heatSources/returnTemperature", rate);
        // scheduleUpdate("/heatSources/systemPressure", rate);

        scheduleUpdate("/system/appliance/actualSupplyTemperature", rate);
        scheduleUpdate("/system/sensors/temperatures/outdoor_t1", rate);
        // scheduleUpdate("/system/sensors/temperatures/return", rate);
        scheduleUpdate("/system/sensors/temperatures/supply_t1", rate);
        scheduleUpdate("/system/sensors/temperatures/switch", rate);
    }

    private void scheduleUpdate(String path, Duration rate) throws Exception {
        var name = "heater" + path.replace('/', '_');
        var help = path;
        var gauge = Gauge.build().name(name).help(help).create();
        gauge.register();
        Callable<Void> update = () -> {
            var value = api.query(path).findValue("value").doubleValue();
            gauge.set(value);
            log.debug("Update {} = {}", path, value);
            return null;
        };
        update.call();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                update.call();
            } catch (Exception e) {
                log.error("Failed to update heater's metric {}", gauge, e);
            }
        }, rate.toSeconds(), rate.toSeconds(), SECONDS);
    }
}
