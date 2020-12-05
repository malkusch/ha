package de.malkusch.ha.monitoring.beta.battery;

import static java.util.Collections.unmodifiableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.malkusch.ha.monitoring.infrastructure.battery.SonnenApi;
import de.malkusch.ha.monitoring.infrastructure.battery.SonnenApi.Status;
import io.prometheus.client.Gauge;

@Service
final class SonnenBattery {

    private final Collection<Consumer<Status>> metrics;
    private final SonnenApi api;

    SonnenBattery(SonnenApi api) throws IOException, InterruptedException {
        var metrics = new ArrayList<Consumer<Status>>();
        metrics.add(registerMetric("batterie_consumption", it -> it.Consumption_W));
        metrics.add(registerMetric("batterie_production", it -> it.Production_W));
        metrics.add(registerMetric("batterie_charge", it -> it.USOC));
        metrics.add(registerMetric("batterie_feed_in", it -> it.GridFeedIn_W));
        metrics.add(registerMetric("batterie_battery_consumption", it -> it.Pac_total_W));
        metrics.add(registerMetric("batterie_battery_voltage", it -> it.Ubat));
        metrics.add(registerMetric("batterie_ac_voltage", it -> it.Uac));
        metrics.add(registerMetric("batterie_capacity", it -> it.RemainingCapacity_W));
        metrics.add(registerMetric("batterie_realCharge", it -> it.RSOC));
        metrics.add(registerMetric("batterie_Sac1", it -> it.Sac1));
        metrics.add(registerMetric("batterie_Sac2", it -> it.Sac2));
        metrics.add(registerMetric("batterie_Sac3", it -> it.Sac3));

        this.metrics = unmodifiableList(metrics);
        this.api = api;

        updateMetrics();
    }

    @Scheduled(fixedRateString = "${sonnen.queryRate}")
    public void updateMetrics() throws IOException, InterruptedException {
        var status = api.status();
        metrics.forEach(it -> it.accept(status));
    }

    private static Consumer<Status> registerMetric(String name, Function<Status, Integer> mapper) {
        var gauge = Gauge.build().name(name).help(name).create();
        gauge.register();
        return status -> gauge.set(mapper.apply(status));
    }
}
