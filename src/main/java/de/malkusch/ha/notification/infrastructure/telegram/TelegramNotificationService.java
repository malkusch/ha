package de.malkusch.ha.notification.infrastructure.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;

import de.malkusch.ha.notification.model.Notification;
import de.malkusch.ha.notification.model.NotificationService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class TelegramNotificationService implements NotificationService {

    private final TelegramBot api;
    private final String chatId;

    @Override
    public void send(Notification notification) {
        api.execute(new SendMessage(chatId, notification.toString()));
    }
}
