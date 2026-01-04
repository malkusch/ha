package de.malkusch.ha.shared.infrastructure.circuitbreaker;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("circuit-breaker")
public record CircuitBreakerConfiguration(int failureThreshold, int successThreshold, Duration delay) {
}
