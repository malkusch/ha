package de.malkusch.ha.automation.infrastructure.light.broadlink;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.malkusch.broadlinkBulb.BroadlinkBulb;
import de.malkusch.broadlinkBulb.BroadlinkBulbFactory;
import de.malkusch.ha.automation.model.light.Light.Api;
import de.malkusch.ha.automation.model.light.LightId;

@Service
public class BroadlinkApiFactory {

    private final BroadlinkBulbFactory broadlinkBulbFactory = new BroadlinkBulbFactory();
    private volatile Map<LightId, BroadlinkBulb> bulbs = new HashMap<>();

    BroadlinkApiFactory() throws IOException {
    }

    public Optional<Api> build(LightId light) {
        return Optional.ofNullable(bulbs.get(light)) //
                .map(BroadlinkApi::new);
    }

    @Scheduled(cron = "59 59 * * * *")
    void discover() throws IOException {
        bulbs = broadlinkBulbFactory.discover().stream() //
                .collect(toMap(BroadlinkApiFactory::lightId, identity()));
    }

    private static LightId lightId(BroadlinkBulb bulb) {
        return new LightId(bulb.mac());
    }
}
