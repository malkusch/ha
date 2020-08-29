package de.malkusch.ha.automation.presentation;

import java.util.Collection;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.dehumidifier.DehumidifierApplicationService;
import de.malkusch.ha.automation.infrastructure.Debouncer.DebounceException;
import de.malkusch.ha.automation.model.NotFoundException;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public final class DehumidifierController {

    private final DehumidifierApplicationService api;

    @PutMapping("/dehumidifier/{id}/on")
    public void turnOn(@PathVariable String id)
            throws NotFoundException, ApiException, InterruptedException, DebounceException {

        var command = new DehumidifierApplicationService.TurnOn(id);
        api.turnOn(command);
    }

    @PutMapping("/dehumidifier/{id}/off")
    public void turnOff(@PathVariable String id)
            throws NotFoundException, ApiException, InterruptedException, DebounceException {

        var command = new DehumidifierApplicationService.TurnOff(id);
        api.turnOff(command);
    }

    @GetMapping("/dehumidifier")
    public Collection<String> list() {
        return api.list();
    }
}
