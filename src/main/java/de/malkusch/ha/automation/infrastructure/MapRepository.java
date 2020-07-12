package de.malkusch.ha.automation.infrastructure;

import static java.util.Optional.ofNullable;

import java.util.Map;

import de.malkusch.ha.automation.model.NotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class MapRepository<ID, ENTITY> {

    private final Map<ID, ENTITY> map;

    public ENTITY find(ID id) throws NotFoundException {
        return ofNullable(map.get(id)).orElseThrow(() -> new NotFoundException(id + " not found"));
    }
}
