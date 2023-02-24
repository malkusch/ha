package de.malkusch.ha.automation.infrastructure.light;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.infrastructure.light.broadlink.BroadlinkApiFactory;
import de.malkusch.ha.automation.model.light.Color;
import de.malkusch.ha.automation.model.light.Light.Api;
import de.malkusch.ha.automation.model.light.LightId;
import de.malkusch.ha.shared.model.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
class ResillientApiFactory {

    private final BroadlinkApiFactory broadlinkApiFactory;

    public Api build(LightId light) {
        return broadlinkApiFactory.build(light) //
                .<Api> map(ResilientApi::new) //
                .orElse(LOGGING_API);
    }

    private static final Api LOGGING_API = new LoggingApi();

    @Slf4j
    private static class LoggingApi implements Api {

        @Override
        public void turnOn() throws ApiException {
            log.info("turnOn");
        }

        @Override
        public void turnOff() throws ApiException {
            log.info("turnOff");
        }

        @Override
        public void changeColor(Color color) throws ApiException {
            log.info("changeColor");
        }
    }

}
