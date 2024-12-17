package de.malkusch.ha.automation.infrastructure.scooter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@ConditionalOnProperty("electricity.scooter.enabled")
@Retention(RetentionPolicy.RUNTIME)
public @interface ScooterEnabled {
}
