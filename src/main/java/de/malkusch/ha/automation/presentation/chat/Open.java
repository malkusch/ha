package de.malkusch.ha.automation.presentation.chat;

import de.malkusch.ha.automation.application.shutters.ShutterApplicationService;
import de.malkusch.ha.shared.model.ApiException;
import de.malkusch.telgrambot.Command;
import de.malkusch.telgrambot.Update.CallbackUpdate;
import de.malkusch.telgrambot.UpdateReceiver.CallbackReceiver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static java.lang.Thread.currentThread;

@Service
@RequiredArgsConstructor
@Slf4j
public final class Open implements CallbackReceiver {

    public static final Command COMMAND = new Command("open");

    private final ShutterApplicationService service;

    @Override
    public Result receive(CallbackUpdate update) {
        var shutter = update.callback().data();
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
