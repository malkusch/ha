package de.malkusch.ha.automation.presentation.chat;

import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.telgrambot.Callback;
import de.malkusch.telgrambot.MessageId;
import de.malkusch.telgrambot.TelegramApi;
import de.malkusch.telgrambot.TelegramApi.Button;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static de.malkusch.ha.automation.model.shutters.ShutterId.KUECHENTUER;
import static de.malkusch.ha.automation.model.shutters.ShutterId.TERRASSE;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
class MenuService implements AutoCloseable {

    private final TelegramApi telegram;
    private volatile MessageId menu;

    @PostConstruct
    public void pinMenu() {
        menu = telegram.send("Menu", //
                openShutterButton("Öffne Küche", KUECHENTUER), //
                openShutterButton("Öffne Terrasse", TERRASSE) //
        );
        telegram.pin(menu);
    }

    private static Button openShutterButton(String name, ShutterId shutter) {
        return new Button(name, new Callback(Open.COMMAND, shutter.toString()));
    }

    @Override
    public void close() throws Exception {
        if (menu == null) {
            return;
        }
        telegram.unpin(menu);
        telegram.delete(menu);
    }
}
