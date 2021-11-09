package de.malkusch.ha.automation.presentation;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.shutters.ShutterApplicationService;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public final class ShutterController {

    private final ShutterApplicationService service;

    @PutMapping("/shutter/{id}/open")
    public void open(@PathVariable String id) throws ApiException, InterruptedException {
        service.openShutter(id);
    }

    @PutMapping("/shutter/{id}/close")
    public void close(@PathVariable String id) throws ApiException, InterruptedException {
        service.closeShutter(id);
    }
}
