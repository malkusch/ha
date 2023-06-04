package de.malkusch.ha.automation.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.shutters.ShutterApplicationService;
import de.malkusch.ha.automation.application.shutters.hotDay.HotDayEventListener;
import de.malkusch.ha.automation.application.shutters.hotDay.HotDayEventListener.CloseShuttersConfiguration;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public final class ShutterController {

    private final ShutterApplicationService service;

    @PutMapping("/shutters/{id}/open")
    public void open(@PathVariable String id) throws ApiException, InterruptedException {
        service.openShutter(id);
    }

    @PutMapping("/shutters/{id}/close")
    public void close(@PathVariable String id) throws ApiException, InterruptedException {
        service.closeShutter(id);
    }

    private final HotDayEventListener hotDayEventListener;

    @GetMapping("/shutters/hot-day-close-configuration")
    public CloseShuttersConfiguration getCloseConfiguration() throws ApiException, InterruptedException {
        return hotDayEventListener.getCloseConfiguration();
    }
}
