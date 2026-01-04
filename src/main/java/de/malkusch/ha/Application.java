package de.malkusch.ha;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class Application {
    public static void main(String[] args) {
        run(Application.class, args);
    }
}
