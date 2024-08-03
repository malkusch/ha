package de.malkusch.ha.automation.presentation.chat;

import de.malkusch.telgrambot.TelegramApi;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import static de.malkusch.telgrambot.UpdateReceiver.onCallback;

@Configuration
@RequiredArgsConstructor
class CommandConfiguration {

    private final Open open;
    private final TelegramApi telegram;

    @PostConstruct
    public void setup() {
        telegram.receiveUpdates( //
                onCallback(Open.COMMAND, open) //
        );
    }
}
