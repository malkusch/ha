package de.malkusch.ha.automation.infrastructure.light;

import org.springframework.stereotype.Service;

import de.malkusch.ha.automation.model.light.Light;
import de.malkusch.ha.automation.model.light.LightId;
import de.malkusch.ha.automation.model.light.LightRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class ResilientLightRepository implements LightRepository {

    private final ResillientApiFactory apiFactory;

    @Override
    public Light find(LightId id) {
        var api = apiFactory.build(id);
        return new Light(id, api);
    }

}
