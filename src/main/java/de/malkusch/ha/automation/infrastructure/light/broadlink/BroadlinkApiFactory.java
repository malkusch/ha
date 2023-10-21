package de.malkusch.ha.automation.infrastructure.light.broadlink;

import java.util.Optional;

import org.springframework.stereotype.Service;

import de.malkusch.broadlinkBulb.BroadlinkBulbFactory;
import de.malkusch.ha.automation.model.light.Light.Api;
import de.malkusch.ha.automation.model.light.LightId;

@Service
public final class BroadlinkApiFactory {

    private final BroadlinkBulbFactory broadlinkBulbFactory = new BroadlinkBulbFactory();

    public Optional<Api> build(LightId light) {
        try {
            var bulb = broadlinkBulbFactory.build(light.id());
            return Optional.of(new BroadlinkApi(bulb));

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
