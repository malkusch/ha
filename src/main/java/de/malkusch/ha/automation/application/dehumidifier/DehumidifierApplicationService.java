package de.malkusch.ha.automation.application.dehumidifier;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.Debouncer.DebounceException;
import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.NotFoundException;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierId;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.FanSpeed;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class DehumidifierApplicationService {

    private final DehumidifierRepository dehumidifiers;

    @RequiredArgsConstructor
    public static final class TurnOn {
        private final String id;
        private final FanSpeed fanSpeed;
    }

    public void turnOn(TurnOn command) throws NotFoundException, ApiException, InterruptedException, DebounceException {
        var id = new DehumidifierId(command.id);
        var dehumidifier = dehumidifiers.find(id);

        dehumidifier.turnOn(command.fanSpeed);
    }

    @RequiredArgsConstructor
    public static final class TurnOff {
        private final String id;
    }

    public void turnOff(TurnOff command)
            throws NotFoundException, ApiException, InterruptedException, DebounceException {

        var id = new DehumidifierId(command.id);
        var dehumidifier = dehumidifiers.find(id);

        dehumidifier.turnOff();
    }
}
