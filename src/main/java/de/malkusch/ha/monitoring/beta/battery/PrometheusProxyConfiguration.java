package de.malkusch.ha.monitoring.beta.battery;

import static de.malkusch.ha.monitoring.infrastructure.PrometheusProxy.mapping;
import static java.util.Arrays.asList;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.malkusch.ha.monitoring.infrastructure.PrometheusProxy;
import de.malkusch.ha.shared.infrastructure.http.HttpClient;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
class PrometheusProxyConfiguration {

    private final HttpClient http;
    private final ObjectMapper mapper;

    @Bean
    public PrometheusProxy sonnenPrometheusProxy(@Value("${sonnen.url}") String url) {
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
        return new PrometheusProxy(url, http, mapper, mappings);
    }

    @Bean
    public PrometheusProxy badPrometheusProxy(@Value("${klima.bad}") String url) {
        var mappings = asList(mapping("/temperature", "bad_temperature"), //
                mapping("/humidity", "bad_humidity") //
        );
        return new PrometheusProxy(url, http, mapper, mappings);
    }

    @Bean
    public PrometheusProxy keller2PrometheusProxy(@Value("${klima.keller2}") String url) {
        var mappings = asList(mapping("/temperature", "keller2_temperature"), //
                mapping("/humidity", "keller2_humidity") //
        );
        return new PrometheusProxy(url, http, mapper, mappings);
    }
}
