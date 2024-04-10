package de.malkusch.ha.notification.infrastructure.telegram;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import de.malkusch.ha.notification.model.NotificationService;
import de.malkusch.telgrambot.TelegramApi;
import lombok.Data;

@Configuration
class TelegramConfiguration {

    @ConfigurationProperties("notification.telegram")
    @Component
    @Data
    static class TelegramProperties {
        private String token;
        private String chatId;
        private Duration timeout;
    }

    @Bean
    NotificationService notificationService(TelegramProperties properties) {
        var api = new TelegramApi(properties.chatId, properties.token, properties.timeout);
        return new TelegramNotificationService(api);
    }
}
