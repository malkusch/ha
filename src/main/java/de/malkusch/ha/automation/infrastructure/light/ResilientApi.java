package de.malkusch.ha.automation.infrastructure.light;

import de.malkusch.ha.automation.model.light.Color;
import de.malkusch.ha.automation.model.light.Light.Api;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
final class ResilientApi implements Api {

    private final Api api;

    @Override
    public void turnOn() {
        call(api::turnOn);
    }

    @Override
    public void turnOff() throws ApiException {
        call(api::turnOff);
    }

    @Override
    public void changeColor(Color color) throws ApiException {
        call(() -> api.changeColor(color));
    }

    private interface Call {
        void call() throws Exception;
    }

    private void call(Call call) {
        try {
            call.call();
        } catch (Exception e) {
            log.warn("Failed calling API", e);
        }
    }
}
