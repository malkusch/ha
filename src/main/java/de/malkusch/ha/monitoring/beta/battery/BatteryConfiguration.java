package de.malkusch.ha.monitoring.beta.battery;

import static de.malkusch.ha.monitoring.infrastructure.PrometheusGaugeProxy.mapping;
import static java.util.Arrays.asList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.monitoring.infrastructure.PrometheusGaugeProxy;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class BatteryConfiguration {

    private final HttpClient http;
    private final ObjectMapper mapper;

    @Bean
    public PrometheusGaugeProxy sonnenPrometheusProxy(@Value("${sonnen.url}") String url) {
        var mappings = asList(mapping("/Consumption_W", "batterie_consumption"), //
                mapping("/Production_W", "batterie_production"), //
                mapping("/USOC", "batterie_charge"), //
                mapping("/GridFeedIn_W", "batterie_feed_in"), //
                mapping("/Pac_total_W", "batterie_battery_consumption"), //
                mapping("/Ubat", "batterie_battery_voltage"), //
                mapping("/Uac", "batterie_ac_voltage"), //
                mapping("/RemainingCapacity_W", "batterie_capacity"), //
                mapping("/RSOC", "batterie_realCharge"), //
                mapping("/Sac1", "batterie_Sac1"), //
                mapping("/Sac2", "batterie_Sac2"), //
                mapping("/Sac3", "batterie_Sac3") //
        );
        return new PrometheusGaugeProxy(url, http, mapper, mappings);
    }
}
