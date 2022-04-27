package de.malkusch.ha.automation.model.climate;

import de.malkusch.ha.automation.model.RoomId;
import de.malkusch.ha.shared.model.ApiException;

public interface ClimateService {

    Humidity humidity(RoomId room) throws ApiException, InterruptedException;

}
