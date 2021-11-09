package de.malkusch.ha.automation.model.shutters;

import java.util.Collection;

public interface ShutterRepository {

    Collection<Shutter> findAll();

    Shutter find(ShutterId id);

}
