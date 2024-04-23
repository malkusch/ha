package de.malkusch.ha.automation.presentation.chat;

import static java.lang.Thread.currentThread;

import java.util.UUID;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.application.shutters.ShutterApplicationService;
import de.malkusch.ha.shared.model.ApiException;
import de.malkusch.telgrambot.Command;
import de.malkusch.telgrambot.Handler.CallbackHandler.Handling;
import de.malkusch.telgrambot.Handler.CallbackHandler.Result;
import de.malkusch.telgrambot.Message.CallbackMessage;
import de.malkusch.telgrambot.TelegramApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public final class Open implements Handling {

    public static final Command COMMAND = new Command("open");

    private final ShutterApplicationService service;

    @Override
    public Result handle(TelegramApi api, CallbackMessage message) {
        var shutter = message.callback().data();
        try {
            service.forceOpenShutter(shutter);
            return new Result(false, "Öffne " + shutter);

        } catch (ApiException e) {
            var reference = UUID.randomUUID();
            log.error("Opening shutter {} failed [{}]", shutter, reference, e);
            return new Result(false, "Opening " + shutter + " failed " + reference);

        } catch (InterruptedException e) {
            currentThread().interrupt();
            log.warn("Opening shutter {} was interrupted", shutter, e);
            return new Result(false, "Öffne " + shutter + " was interrupted");
        }
    }
}
