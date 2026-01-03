package de.malkusch.ha.notification.infrastructure.telegram;

import de.malkusch.ha.notification.model.NotificationService;
import de.malkusch.telgrambot.TelegramApi;
import de.malkusch.telgrambot.api.TelegramApiFactory;
import de.malkusch.telgrambot.api.Timeouts;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.time.Duration;

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
    TelegramApi telegramApi(TelegramProperties properties) {
        var timeouts = new Timeouts(properties.timeout);
        return TelegramApiFactory.telegramApi(properties.chatId, properties.token, timeouts);
    }

    @Bean
    NotificationService notificationService(TelegramApi api) {
        return new TelegramNotificationService(api);
    }
}
