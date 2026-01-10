package de.malkusch.ha.automation.presentation.chat;

import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.telgrambot.Callback;
import de.malkusch.telgrambot.MessageId;
import de.malkusch.telgrambot.TelegramApi;
import de.malkusch.telgrambot.TelegramApi.Button;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static de.malkusch.ha.automation.model.shutters.ShutterId.KUECHENTUER;
import static de.malkusch.ha.automation.model.shutters.ShutterId.TERRASSE;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
@Slf4j
class MenuService implements AutoCloseable {

    private final TelegramApi telegram;
    private volatile MessageId menu;

    @PostConstruct
    public void pinMenu() {
        menu = telegram.sendSilently("Menu", //
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
        var menu = this.menu;
        this.menu = null;
        if (menu == null) {
            return;
        }
        try {
            telegram.unpin(menu);

        } catch (Throwable e) {
            log.warn("Failed unpinning {} - unpinning all", menu, e);
            telegram.unpin();

        } finally {
            telegram.delete(menu);
        }
    }
}
