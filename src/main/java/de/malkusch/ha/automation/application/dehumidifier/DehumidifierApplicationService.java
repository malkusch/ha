package de.malkusch.ha.automation.application.dehumidifier;

import static java.util.stream.Collectors.toList;

import java.util.Collection;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.Debouncer.DebounceException;
import de.malkusch.ha.automation.model.NotFoundException;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierId;
import de.malkusch.ha.automation.model.dehumidifier.Dehumidifier.DehumidifierRepository;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class DehumidifierApplicationService {

    private final DehumidifierRepository dehumidifiers;

    @RequiredArgsConstructor
    public static final class TurnOn {
        private final String id;
    }

    public void turnOn(TurnOn command) throws NotFoundException, ApiException, InterruptedException, DebounceException {
        var id = new DehumidifierId(command.id);
        var dehumidifier = dehumidifiers.find(id);

        dehumidifier.turnOn();
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

    public Collection<String> list() {
        return dehumidifiers.findAll().stream().map(it -> it.id.getId()).collect(toList());
    }
}
