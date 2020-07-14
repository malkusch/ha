package de.malkusch.ha.automation.presentation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.dehumidifier.DehumidifierApplicationService;
import de.malkusch.ha.automation.model.ApiException;
import de.malkusch.ha.automation.model.Dehumidifier.FanSpeed;
import de.malkusch.ha.automation.model.NotFoundException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public final class DehumidifierController {

    private final DehumidifierApplicationService api;

    @PutMapping("/dehumidifier/{id}/on")
    public void turnOn(@PathVariable String id, @RequestParam FanSpeed fanSpeed)
            throws NotFoundException, ApiException, InterruptedException {

        var command = new DehumidifierApplicationService.TurnOn(id, fanSpeed);
        api.turnOn(command);
    }

    @PutMapping("/dehumidifier/{id}/off")
    public void turnOff(@PathVariable String id) throws NotFoundException, ApiException, InterruptedException {
        var command = new DehumidifierApplicationService.TurnOff(id);
        api.turnOff(command);
    }
}
