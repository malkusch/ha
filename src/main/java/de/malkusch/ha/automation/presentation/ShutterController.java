package de.malkusch.ha.automation.presentation;

import static de.malkusch.ha.shared.infrastructure.DateUtil.formatTime;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import de.malkusch.ha.automation.application.shutters.ScheduleWindProtectionApplicationService;
import de.malkusch.ha.automation.application.shutters.ScheduleWindProtectionApplicationService.WindProtectionState;
import de.malkusch.ha.automation.application.shutters.ShutterApplicationService;
import de.malkusch.ha.automation.application.shutters.hotDay.HotDayEventListener;
import de.malkusch.ha.automation.application.shutters.hotDay.HotDayEventListener.CloseShuttersConfiguration;
import de.malkusch.ha.automation.model.weather.Weather;
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

    private final Weather weather;
    private final HotDayEventListener hotDayEventListener;
    private final ScheduleWindProtectionApplicationService scheduleWindProtectionService;

    @GetMapping("/shutters")
    public Shutters getShutters() throws ApiException, InterruptedException {
        var closeShuttersConfiguration = hotDayEventListener.getCloseConfiguration();
        var windProtectionState = scheduleWindProtectionService.windProtectionState();
        var weatherUpdate = formatTime(weather.lastUpdate());

        return new Shutters(weatherUpdate, closeShuttersConfiguration, windProtectionState);
    }

    public static record Shutters(String weatherUpdate, CloseShuttersConfiguration closeShuttersConfiguration,
            WindProtectionState windProtectionState) {
    }
}
