package de.malkusch.ha.automation.infrastructure.light;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.malkusch.broadlinkBulb.BroadlinkBulbFactory;
import de.malkusch.ha.automation.model.light.Light;
import de.malkusch.ha.automation.model.light.LightId;
import de.malkusch.ha.automation.model.light.LightRepository;

@Service
final class BroadlinkLightRepository implements LightRepository {

    private final Map<LightId, Light> lights = new HashMap<>();;

    BroadlinkLightRepository() throws IOException {
        var factory = new BroadlinkBulbFactory();
        for (var bulb : factory.discover()) {
            var api = new ResilientApi(new BroadlinkLightApi(bulb));
            var id = new LightId(bulb.mac());
            var light = new Light(id, api);
            lights.put(id, light);
        }
    }

    @Override
    public Light find(LightId id) {
        return lights.get(id);
    }
}
