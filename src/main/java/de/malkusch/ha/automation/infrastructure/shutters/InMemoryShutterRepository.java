package de.malkusch.ha.automation.infrastructure.shutters;

import static java.util.stream.Collectors.toUnmodifiableMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.malkusch.ha.automation.model.shutters.Shutter;
import de.malkusch.ha.automation.model.shutters.ShutterId;
import de.malkusch.ha.automation.model.shutters.ShutterRepository;

final class InMemoryShutterRepository implements ShutterRepository {

    private final Map<ShutterId, Shutter> shutters;

    InMemoryShutterRepository(List<Shutter> shutters) {
        this.shutters = shutters.stream().collect(toUnmodifiableMap(it -> it.id, it -> it));
    }

    @Override
    public Collection<Shutter> findAll() {
        return shutters.values();
    }

    @Override
    public Shutter find(ShutterId id) {
        return shutters.get(id);
    }
}
