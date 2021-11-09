package de.malkusch.ha.automation.application.shutters;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public final class ShutterApplicationService {

    private final ShutterRepository shutters;

    public void openShutter(String shutterId) throws ApiException, InterruptedException {
        var shutter = shutters.find(ShutterId.valueOf(shutterId));
        shutter.open();
    }

    public void closeShutter(String shutterId) throws ApiException, InterruptedException {
        var shutter = shutters.find(ShutterId.valueOf(shutterId));
        shutter.close();
    }
}
