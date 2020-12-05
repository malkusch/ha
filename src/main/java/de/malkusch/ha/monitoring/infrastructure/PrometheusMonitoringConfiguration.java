package de.malkusch.ha.monitoring.infrastructure;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.prometheus.client.exporter.MetricsServlet;

@Configuration
class PrometheusMonitoringConfiguration {

    @Bean
    public ServletRegistrationBean<MetricsServlet> prometheusServlet() {
        return new ServletRegistrationBean<>(new MetricsServlet(), "/prometheus/*");
    }
}
